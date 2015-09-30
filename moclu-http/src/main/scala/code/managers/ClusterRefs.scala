package code.managers

import com.github.antidata.actors.HtmMasterActor
import com.github.antidata.bootstrap.Boot

object ClusterRefs {
  lazy val actorSystem = {
    import com.github.antidata.managers.AppConfiguration
    val port = AppConfiguration.values.getString("akka.remote.netty.tcp.port")
    Boot.startup(Seq(port))
    Boot.systemRef.actorOf(akka.actor.Props[HtmMasterActor], "master"+net.liftweb.util.StringHelpers.randomString(30))
  }
}
