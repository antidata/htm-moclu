package code.managers

import com.github.antidata.actors.HtmMasterActor
import com.github.antidata.bootstrap.Boot

object ClusterRefs {
  lazy val actorSystem = {
    import com.github.antidata.managers.AppConfiguration
    val hostname = AppConfiguration.values.getString("akka.remote.netty.tcp.hostname")
    val port = AppConfiguration.values.getString("akka.remote.netty.tcp.port")
    Boot.startup(Seq(if(hostname != "127.0.0.1") port else "2552")) // If localhost there is a huge chance to be running the seed on port 2551
    Boot.systemRef.actorOf(akka.actor.Props[HtmMasterActor], "master"+net.liftweb.util.StringHelpers.randomString(30))
  }
}
