package com.github.antidata.actors

import akka.actor.{Actor, ActorLogging}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._

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
      log.debug("Member is Up: {}", member.address)

    case UnreachableMember(member) =>
      log.debug("Member detected as unreachable: {}", member)

    case MemberRemoved(member, previousStatus) =>
      log.debug("Member is Removed: {} after {}", member.address, previousStatus)

    case _: MemberEvent => // ignore

    case m => println(s"received $m")
  }
}
