package com.github.antidata.actors

import akka.actor.{ActorRef, Actor, ActorLogging, Props}
import akka.pattern.ask
import akka.cluster.sharding.ShardRegion
import akka.persistence.{RecoveryCompleted, PersistentActor}
import com.github.antidata.managers.HtmModelsManager.HtmModelsManagerEvent
import com.github.antidata.managers._
import com.github.antidata.model._
import org.numenta.nupic.network.Inference
import rx.Subscriber
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

object HtmModelActor {
  def props(): Props = Props(new HtmModelActor())

  val idExtractor: ShardRegion.ExtractEntityId = {
    case event: ClusterEvent => (event.HtmModelId, event)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case event: ClusterEvent => (math.abs(event.HtmModelId.hashCode) % 100).toString
  }

  val shardName = "HtmModelShard"


  trait ClusterEvent {
    val HtmModelId: String
  }

  case class CreateHtmModel(HtmModelId: String) extends ClusterEvent // TODO add min max parameters
  case class HtmEventGetModel(HtmModelId: String) extends ClusterEvent
  case class HtmModelEvent(HtmModelId: String, htmModelEventData: HtmModelEventData) extends ClusterEvent
  case class BulkHtmModelEvent(HtmModelId: String, htmModelEventData: HtmModelEventData) extends ClusterEvent
  case class CreateModelOk(HtmModelId: String) extends ClusterEvent
  case class CreateModelFail(HtmModelId: String) extends ClusterEvent
  case class GetModelData(HtmModelId: String, data: List[HtmModelData]) extends ClusterEvent
  case class ModelNotFound(HtmModelId: String) extends ClusterEvent
  case class ModelPrediction(HtmModelId: String, anomalyScore: Double, prediction: Any) extends ClusterEvent
  case class HtmModelEventData(HtmModelId: String, value: String, timestamp: String) extends ClusterEvent
  case class HtmModel(HtmModelId: String, data: List[HtmModelData], network: HtmModelNetwork) extends ClusterEvent
  case class HtmModelData(HtmModelId: String, value: String, timestamp: Long, anomalyScore: Option[Double]) extends ClusterEvent
  case class DelayedHtmModelEvent(HtmModelId: String, event: HtmModelEvent, count: Int) extends ClusterEvent
  case class ResetNetwork(HtmModelId: String) extends ClusterEvent

  def filterModelData(id: String, value: String, time: String)(htmModelData: HtmModelData): Boolean = {
    id == htmModelData.HtmModelId &&
    value == htmModelData.value &&
    (DatesManager.parseLong(time).getOrElse(0L) * 1000L) == htmModelData.timestamp
  }
}

class HtmModelActor extends PersistentActor with ActorLogging {
  import akka.cluster.Cluster
  import HtmModelActor._
  import com.github.antidata.actors.HtmModelActor._

  implicit val timeout = akka.util.Timeout(60L, java.util.concurrent.TimeUnit.SECONDS)

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  private var state = HtmModelState("", "", "")

  val from = Cluster(context.system).selfAddress.hostPort

  case class HtmModelState(id: String, value: String, timestamp: String) {
    def created(id: String) = copy(id = id)
    def event(value: String, timestamp: String) = copy(value = value, timestamp = timestamp)
  }

  private def applyModel(id: String)(apply: Option[HtmModel] => Unit) = {
    apply(HtmModelsManager.actorInstance.getModel(HtmModelId(id)))
//    (HtmModelsManager() ? HtmModelsManager.GetModel(HtmModelId(id))).mapTo[HtmModelsManagerEvent] onSuccess {
//      case HtmModelsManager.ModelResponse(modelOption) => apply(modelOption)
//      case _ => println(s"No retorno modl ")
//    }
  }

  def updateState(sender: Option[ActorRef]): DomainEvent => Unit = {
    case HtmModelCreated(id) =>
      state = state.created(id)
      // TODO refactor code below
      val htmModel = HtmGeoModelFactory()
      HtmModelsManager() ! HtmModelsManager.AddModel(HtmModel(id, Nil, htmModel))

    case e@HtmModeledEvent(v, t) =>
      state = state.event(v, t)
      val filterDef = filterModelData(state.id, v, t) _

      applyModel(state.id) {
        case Some(htmModel) if !htmModel.data.exists(filterDef) =>
          log.info(s"Sending to publisher: $t, $v")
          val millisOpt = DatesManager.parseLong(t)
          millisOpt.foreach { millis =>
            htmModel.network.net.observe().subscribe(new Subscriber[Inference]() {
              var applied = false

              def onNext(i: Inference) {
                if (applied) return // These observables fails at some point if we don't check for applied
                applied = true
                HtmModelsManager() ! HtmModelsManager.UpdateModel(htmModel.copy(data = List(HtmModelData(htmModel.HtmModelId, v, millis * 1000L, Some(i.getAnomalyScore)))))

                sender.foreach(_ ! ModelPrediction(htmModel.HtmModelId, i.getAnomalyScore, "0"))
                //capturedSender ! ModelPrediction(htmModel.HtmModelId, i.getAnomalyScore, i.getClassification("location").getMostProbableValue(1))
              }

              override def onError(throwable: Throwable): Unit = log.error(throwable.getMessage)

              override def onCompleted(): Unit = {}
            })
            htmModel.network.publisher.onNext(s"${DatesManager.toDateTime(millis * 1000L)}, $v")
          }

        case _ =>
          log.info(s"HtmModelsManager should contain model ${state.id}")
      }

    case e@HtmModelResetted(id) =>
      HtmModelsManager() ! HtmModelsManager.ResetNetwork(id)

  }

  override def receiveRecover: Receive = {
    case e: DomainEvent => updateState(None)(e)

    case RecoveryCompleted =>
      log.info(s"Recovery completed for model ${state.id}")
  }

  val receiveCommand: Receive = {
    case CreateHtmModel(id) =>
      log.debug(s"$id added from $from")
      persist(HtmModelCreated(id))(updateState(None))
      sender() ! CreateModelOk(id)

    case HtmEventGetModel(hmi) =>
      applyModel(hmi) {
        case Some(htmModel) =>
          log.debug(s"data ${htmModel.data} from $from")
          sender() ! GetModelData(hmi, htmModel.data)
        case _ =>
          log.info(s"$hmi not found from $from")
          sender() ! ModelNotFound(hmi)
      }

    case e@HtmModelEvent(id, hmed) =>
      log.info(s"Received HtmModelEvent($id, $hmed)")
      val capturedSender = sender()
      persist(HtmModeledEvent(hmed.value, hmed.timestamp))(updateState(Some(capturedSender)))

    case e@BulkHtmModelEvent(id, hmed) =>
      log.info(s"Received BulkHtmModelEvent($id, $hmed)")
      val capturedSender = sender()
      applyModel(hmed.HtmModelId) {
        case Some(htmModel) =>
          log.debug(s"Sending to publisher: ${hmed.timestamp},${hmed.value} from $from")
          persist(HtmModeledEvent(hmed.value, hmed.timestamp))(updateState(None))

        case _ =>
          // If the model is not yet in the manager then wait delaying the event
          val dEvent = DelayedHtmModelEvent(id, e, 1)
          context.system.scheduler.scheduleOnce(scala.concurrent.duration.FiniteDuration(30L, scala.concurrent.duration.SECONDS), self, dEvent)
          log.debug(s"${hmed.HtmModelId} not found from $from delayed event")
          capturedSender ! ModelNotFound(hmed.HtmModelId)
      }

    case d@DelayedHtmModelEvent(id, e, c) =>
      log.debug(s"Received DelayedHtmModelEvent($id, ${e.htmModelEventData})")
      applyModel(id) {
        case Some(htmModel) =>
          persist(HtmModeledEvent(e.htmModelEventData.value, e.htmModelEventData.timestamp))(updateState(None))

        case _ =>
          // If the model is not yet in the manager then wait delaying the event
          val dEvent = DelayedHtmModelEvent(id, e, c+1)
          context.system.scheduler.scheduleOnce(scala.concurrent.duration.FiniteDuration(30L, scala.concurrent.duration.SECONDS), self, dEvent)
      }

    case r@ResetNetwork(id) =>
      persist(HtmModelResetted(id))(updateState(None))
  }

  implicit def bulk2Event(bulk: BulkHtmModelEvent): HtmModelEvent = HtmModelEvent(bulk.HtmModelId, bulk.htmModelEventData)
}
