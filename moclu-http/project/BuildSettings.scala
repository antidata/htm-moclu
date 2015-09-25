import com.earldouglas.xsbtwebplugin.WebPlugin
import sbt._
import sbt.Keys._
import com.earldouglas.xsbtwebplugin.WebPlugin.{container, webSettings}
import com.earldouglas.xsbtwebplugin.PluginKeys._
import sbtbuildinfo.Plugin._
import less.Plugin._
import sbtbuildinfo.Plugin._
import sbtclosure.SbtClosurePlugin._

object BuildSettings {

  val buildTime = SettingKey[String]("build-time")

  val basicSettings = Defaults.defaultSettings ++ Seq(
    name := "moclu-hhtp",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.7",
    scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-language:postfixOps", "-language:implicitConversions"),
    resolvers ++= Dependencies.resolutionRepos ++ Seq("Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository")
  )

  val liftAppSettings = basicSettings ++
    webSettings ++
    buildInfoSettings ++
    seq(
      buildTime := System.currentTimeMillis.toString,

      // build-info
      buildInfoKeys ++= Seq[BuildInfoKey](buildTime),
      buildInfoPackage := "code",
      sourceGenerators in Compile <+= buildInfo

    )

  lazy val noPublishing = seq(
    publish := (),
    publishLocal := ()
  )
}


