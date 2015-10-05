import com.earldouglas.xsbtwebplugin.WebPlugin
import sbt._
import sbt.Keys._
import com.earldouglas.xsbtwebplugin.WebPlugin.{container, webSettings}
import sbtassembly.MergeStrategy
import sbtbuildinfo.Plugin._

object BuildSettings {

  val buildTime = SettingKey[String]("build-time")

  val basicSettings = Defaults.defaultSettings ++ Seq(
    name := "moclu-http",
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
      sourceGenerators in Compile <+= buildInfo,
      mainClass in Compile := Some("code.WebServerStarter"),
      test in sbtassembly.AssemblyKeys.assembly := {},
      resourceGenerators in Compile <+= (resourceManaged, baseDirectory) map { (managedBase, base) =>
      val webappBase = base / "src" / "main" / "webapp"
      for {
        (from, to) <- webappBase ** "*" x rebase(webappBase, managedBase / "main" / "webapp")
      } yield {
        Sync.copy(from, to)
        to
      }
    })

  lazy val noPublishing = seq(
    publish := (),
    publishLocal := ()
  )
}


