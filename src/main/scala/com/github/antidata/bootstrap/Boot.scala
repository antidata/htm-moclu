package com.github.antidata.bootstrap

import akka.actor._
import com.github.antidata.actors.{HtmMasterActor, HtmModelActor, HtmModelsClusterListener}
import com.github.antidata.managers.HtmModelsManager
import com.typesafe.config.ConfigFactory
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}

object Boot {
  var systemRef: ActorSystem = null
  def startup(ports: Seq[String]) = {
    ports foreach { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("HtmModelsSystem", config)

      val shardSettings = ClusterShardingSettings(system)//.withRole("")

      ClusterSharding(system).start(
        typeName = HtmModelActor.shardName,
        entityProps = HtmModelActor.props(),
        settings = shardSettings,
        extractEntityId = HtmModelActor.idExtractor,
        extractShardId = HtmModelActor.shardResolver
      )
      systemRef = system
    }
  }

  def main(args: Array[String]): Unit = {
    HtmModelsManager.init()
    startup(Seq("2551"/*, "2552", "0"*/))
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
