package com.github.antidata.actors

import akka.actor.{ActorLogging, Actor}
import akka.cluster.sharding.ClusterSharding
import scala.concurrent.duration._
import com.github.antidata.actors.HtmModelActor._
import akka.pattern.ask
import akka.util.Timeout

object HtmMasterActor {

}

class HtmMasterActor extends Actor with ActorLogging {
  import context.dispatcher
  val htmModelsRegion = ClusterSharding(context.system).shardRegion(HtmModelActor.shardName)
  implicit val timeout = akka.util.Timeout(10L, java.util.concurrent.TimeUnit.SECONDS)

  def receive = {
    case event =>
      log.info(s"Processing $event")
      val thisSender = sender()
      (htmModelsRegion ? event).mapTo[ClusterEvent]
        .onSuccess {
        case response =>
          log.info(s"Repying $response")
          thisSender ! response
      }
  }
}