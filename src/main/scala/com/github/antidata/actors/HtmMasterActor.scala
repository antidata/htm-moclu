package com.github.antidata.actors

import akka.actor.{ActorLogging, Actor}
import com.github.antidata.actors.messages.CreateModelOk
import com.github.antidata.events.CreateHtmModel
import akka.cluster.sharding.ClusterSharding
import scala.concurrent.duration._

object HtmMasterActor {

}

class HtmMasterActor extends Actor with ActorLogging {
  import context.dispatcher
  val tickTask = context.system.scheduler.schedule(3.seconds, 3.seconds, self, CreateHtmModel("123"))
  val htmModelsRegion = ClusterSharding(context.system).shardRegion(HtmModelActor.shardName)

  def receive = {
    case e@CreateHtmModel(_) =>
      htmModelsRegion ! e

    case e@CreateModelOk(id) =>
      println(s"$id")

    case e =>
      log.debug(s"$e")
  }
}