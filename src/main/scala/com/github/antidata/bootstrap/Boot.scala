package com.github.antidata.bootstrap

import akka.actor._
import com.github.antidata.actors.{HtmMasterActor, HtmModelActor, HtmModelsClusterListener}
import com.github.antidata.managers.HtmModelsManager
import com.typesafe.config.ConfigFactory
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}

object Boot {
  var systemRef: ActorSystem = null
  def startup(ports: Seq[String]) = {
    ports map { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("HtmModelsSystem", config)

      val shardSettings = ClusterShardingSettings(system)//.withRole("")

      val aaa = ClusterSharding(system).start(
        typeName = HtmModelActor.shardName,
        entityProps = HtmModelActor.props(),
        settings = shardSettings,
        extractEntityId = HtmModelActor.idExtractor,
        extractShardId = HtmModelActor.shardResolver
      )
//      if(port != "2551") {
//        val p = system.actorOf(Props[HtmMasterActor], "master")
//        p ! CreateHtmModel("123")
//      }
      systemRef = system
      aaa
    }
  }
  var systems: Seq[ActorRef] = null
  def main(args: Array[String]): Unit = {
    HtmModelsManager.init()
    systems = startup(Seq("2552"/*, "2552", "0"*/))
  }

  def startupWeb(ports: Seq[String]) = {
    ports map { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("ClusterSystem", config)
      // Create an actor that handles cluster domain events
      system.actorOf(Props[HtmModelsClusterListener], name = "htmModelsClusterListener2")
    }
  }
}
