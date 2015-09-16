package com.github.antidata.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.persistence.{RecoveryCompleted, PersistentActor}
import com.github.antidata.actors.messages._
import com.github.antidata.events.{HtmModelEvent, HtmEventGetModel, CreateHtmModel}
import com.github.antidata.managers.{HtmModelFactory, HtmModelsManager}
import com.github.antidata.model._
import org.numenta.nupic.network.Inference
import rx.Subscriber

object HtmModelActor {
  def props(): Props = Props(new HtmModelActor())

  val idExtractor: ShardRegion.ExtractEntityId = {
    case event: ClusterEvent => (event.HtmModelId, event)
  }

  val shardResolver: ShardRegion.ExtractShardId = {
    case event: ClusterEvent => (math.abs(event.HtmModelId.hashCode) % 100).toString
  }

  val shardName = "HtmModelShard"

}

class HtmModelActor extends PersistentActor with ActorLogging {
  import akka.cluster.Cluster
  import HtmModelActor._
  import com.github.antidata.actors.messages._

  override def persistenceId: String = self.path.parent.name + "-" + self.path.name // TODO Check this

  private var state = HtmModelState("", "")

  val from = Cluster(context.system).selfAddress.hostPort

  case class HtmModelState(id: String, timestamp: String) {

  }

  override def receiveRecover: Receive = {
    case HtmModelCreated(id) =>
      state = HtmModelState(id, "")

    case RecoveryCompleted =>
      log.info("Calculator recovery completed")
  }

  val receiveCommand: Receive = {
    case CreateHtmModel(id) =>
      val htmModel = HtmModelFactory()
      HtmModelsManager.addModel(HtmModel(id, Nil, htmModel)) match {
        case None =>
          log.debug(s"$id added from $from")
          println(s"$id added from $from")
          sender() ! CreateModelOk(id)
        case Some(_) =>
          log.debug(s"$id exists from $from")
          sender() ! CreateModelFail(id)
      }

    case HtmEventGetModel(hmi) =>
      HtmModelsManager.getModel(HtmModelId(hmi)) match {
        case Some(htmModel) =>
          log.debug(s"data ${htmModel.data} from $from")
          sender() ! GetModelData(hmi, htmModel.data)
        case _ =>
          log.info(s"$hmi not found from $from")
          sender() ! ModelNotFound(hmi)
      }

    case HtmModelEvent(hmed) =>
      val capturedSender = sender()
      HtmModelsManager.getModel(HtmModelId(hmed.HtmModelId)) match {
        case Some(htmModel) =>
          log.info(s"Sending to publisher: ${hmed.timestamp},${hmed.value} from $from")
          htmModel.network.net.observe().subscribe(new Subscriber[Inference]() {
            def onNext(i: Inference) {
              this.unsubscribe()
              println(ModelPrediction(htmModel.HtmModelId, i.getAnomalyScore, i.getClassification("consumption")))
              HtmModelsManager.updateModel(
                htmModel.copy(data = HtmModelData(htmModel.HtmModelId, hmed.value, 0L /*TODO get datetime timestamp*/, Some(i.getAnomalyScore)) :: htmModel.data))
              capturedSender ! ModelPrediction(htmModel.HtmModelId, i.getAnomalyScore, i.getClassification("consumption"))
            }
            override def onError(throwable: Throwable): Unit = log.error(throwable.getMessage)
            override def onCompleted(): Unit = {}
          })
          htmModel.network.publisher.onNext(s"${hmed.timestamp},${hmed.value}")

        case _ =>
          log.info(s"${hmed.HtmModelId} not found from $from")
          capturedSender ! ModelNotFound(hmed.HtmModelId)
      }
  }
}
