package com.github.antidata.bootstrap

import akka.actor._
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import akka.util.Timeout
import com.github.antidata.actors.{HtmMasterActor, HtmModelActor, HtmModelsClusterListener}
import com.github.antidata.events.CreateHtmModel
import com.github.antidata.managers.HtmModelsManager
import com.typesafe.config.ConfigFactory
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import scala.concurrent.duration._
import akka.pattern.ask

object Boot {
  def startup(ports: Seq[String]) = {
    ports map { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("HtmModelsSystem", config)
      // Create an actor that handles cluster domain events
//      val clusterActor = system.actorOf(Props[HtmModelsClusterListener], name = "htmModelsClusterListener")
      startupSharedJournal(system, startStore = (port == "2551"), path =
        ActorPath.fromString("akka.tcp://HtmModelsSystem@127.0.0.1:2551/user/store"))

      val shardSettings = ClusterShardingSettings(system)//.withRole("")

      val aaa = ClusterSharding(system).start(
        typeName = HtmModelActor.shardName,
        entityProps = HtmModelActor.props(),
        settings = shardSettings,
        extractEntityId = HtmModelActor.idExtractor,
        extractShardId = HtmModelActor.shardResolver
      )
      if(port != "2551") {
        val p = system.actorOf(Props[HtmMasterActor], "master")
        p ! CreateHtmModel("123")
      }
      aaa
  //    clusterActor
    }
  }
  var systems: Seq[ActorRef] = null
  def main(args: Array[String]): Unit = {
    HtmModelsManager.init()
    systems = startup(Seq("2551", "2552"/*, "0"*/))
  }

  def startupSharedJournal(system: ActorSystem, startStore: Boolean, path: ActorPath): Unit = {
    // Start the shared journal one one node (don't crash this SPOF)
    // This will not be needed with a distributed journal
    if (startStore)
      system.actorOf(Props[SharedLeveldbStore], "store")
    // register the shared journal
    import system.dispatcher
    implicit val timeout = Timeout(15.seconds)
    val f = system.actorSelection(path) ? Identify(None)
    f.onSuccess {
      case ActorIdentity(_, Some(ref)) => SharedLeveldbJournal.setStore(ref, system)
      case _ =>
        system.log.error("Shared journal not started at {}", path)
        system.terminate()
    }
    f.onFailure {
      case _ =>
        system.log.error("Lookup of shared journal at {} timed out", path)
        system.terminate()
    }
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
