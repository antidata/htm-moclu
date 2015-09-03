package com.github.antidata.bootstrap

import akka.actor.{ActorRef, ActorSystem, Props}
import com.github.antidata.actors.HtmModelsClusterListener
import com.github.antidata.managers.HtmModelsManager
import com.typesafe.config.ConfigFactory

object Boot {
  def startup(ports: Seq[String]) = {
    ports map { port =>
      // Override the configuration of the port
      val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).withFallback(ConfigFactory.load())

      // Create an Akka system
      val system = ActorSystem("ClusterSystem", config)
      // Create an actor that handles cluster domain events
      system.actorOf(Props[HtmModelsClusterListener], name = "htmModelsClusterListener")
    }
  }
  var systems: Seq[ActorRef] = null
  def main(args: Array[String]): Unit = {
    HtmModelsManager.init()
    systems = startup(Seq("2551", "2552", "0"))
  }
}
