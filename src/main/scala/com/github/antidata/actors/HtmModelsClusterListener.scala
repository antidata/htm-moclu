package com.github.antidata.actors

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import com.github.antidata.events.{CreateHtmModel, HtmEventGetModel, HtmModelEvent}
import com.github.antidata.managers.{HtmModelsManager, HtmModelFactory}
import com.github.antidata.model.{HtmModelId, HtmModel}

class HtmModelsClusterListener extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  // subscribe to cluster changes, re-subscribe when restart 
  override def preStart(): Unit = {
    //#subscribe
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)

    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)

    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}", member.address, previousStatus)

    case _: MemberEvent => // ignore

    // TODO move this to separate actors
    case CreateHtmModel(id) =>
      val htmModel = HtmModelFactory()
      HtmModelsManager.addModel(HtmModel(HtmModelId(id), Nil, htmModel))
      sender() ! s"$id added" // TODO Make case class to reply

    case HtmEventGetModel(hmi) =>
      HtmModelsManager.getModel(hmi) match {
        case Some(htmModel) =>
          sender() ! htmModel.data // TODO Make case class to reply
        case _ =>
          log.info(s"$hmi not found")
          sender() ! s"$hmi not found" // TODO Make case class to reply
      }

    case HtmModelEvent(hmed) =>
      HtmModelsManager.getModel(hmed.modelId) match {
        case Some(htmModel) =>
          log.info(s"Sending to publisher: ${hmed.timestamp},${hmed.value}")
          htmModel.network.publisher.onNext(s"${hmed.timestamp},${hmed.value}")
          // TODO reply with prediction
        case _ =>
          sender() ! s"${hmed.modelId} not found" // TODO Make case class to reply
          log.info(s"${hmed.modelId} not found")
      }

    case m => println(s"received $m")
  }
}
