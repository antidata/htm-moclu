package com.github.antidata.actors

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import com.github.antidata.actors.messages._
import com.github.antidata.events.{CreateHtmModel, HtmEventGetModel, HtmModelEvent}
import com.github.antidata.managers.{HtmModelsManager, HtmModelFactory}
import com.github.antidata.model.{HtmModelData, HtmModelId, HtmModel}
import org.numenta.nupic.network.Inference
import rx.Subscriber

class HtmModelsClusterListener extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  // subscribe to cluster changes, re-subscribe when restart 
  override def preStart(): Unit = {
    //#subscribe
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case CreateHtmModel(id) =>
      val htmModel = HtmModelFactory()
      HtmModelsManager.addModel(HtmModel(HtmModelId(id), Nil, htmModel)) match {
        case None =>
          log.debug(s"$id added")
          sender() ! CreateModelOk(id)
        case Some(_) =>
          log.debug(s"$id exists")
          sender() ! CreateModelFail(id)
      }

    case HtmEventGetModel(hmi) =>
      HtmModelsManager.getModel(hmi) match {
        case Some(htmModel) =>
          log.debug(s"data ${htmModel.data}")
          sender() ! GetModelData(hmi.id, htmModel.data)
        case _ =>
          log.info(s"$hmi not found")
          sender() ! ModelNotFound(hmi.id)
      }

    case HtmModelEvent(hmed) =>
      val capturedSender = sender()
      HtmModelsManager.getModel(hmed.modelId) match {
        case Some(htmModel) =>
          log.info(s"Sending to publisher: ${hmed.timestamp},${hmed.value}")
          htmModel.network.net.observe().subscribe(new Subscriber[Inference]() {
            def onNext(i: Inference) {
              this.unsubscribe()
              println(ModelPrediction(htmModel.id.id, i.getAnomalyScore))
              HtmModelsManager.updateModel(
                htmModel.copy(data = HtmModelData(hmed.value, 0L /*TODO get datetime timestamp*/, Some(i.getAnomalyScore)) :: htmModel.data))
              capturedSender ! ModelPrediction(htmModel.id.id, i.getAnomalyScore)
            }
            override def onError(throwable: Throwable): Unit = log.error(throwable.getMessage)
            override def onCompleted(): Unit = {}
          })
          htmModel.network.publisher.onNext(s"${hmed.timestamp},${hmed.value}")

        case _ =>
          log.info(s"${hmed.modelId} not found")
          capturedSender ! ModelNotFound(hmed.modelId.id)
      }

    case MemberUp(member) =>
      log.debug("Member is Up: {}", member.address)

    case UnreachableMember(member) =>
      log.debug("Member detected as unreachable: {}", member)

    case MemberRemoved(member, previousStatus) =>
      log.debug("Member is Removed: {} after {}", member.address, previousStatus)

    case _: MemberEvent => // ignore

    case m => println(s"received $m")
  }
}
