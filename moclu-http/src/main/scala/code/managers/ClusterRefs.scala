package code.managers

import com.github.antidata.actors.HtmMasterActor
import com.github.antidata.bootstrap.Boot

object ClusterRefs {
  lazy val actorSystem = {
    Boot.startup(Seq("2551"))
    Boot.systemRef.actorOf(akka.actor.Props[HtmMasterActor], "master"+net.liftweb.util.StringHelpers.randomString(30))
  }
}
