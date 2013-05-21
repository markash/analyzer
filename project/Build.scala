import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "analyzer"
  val appVersion      = "0.1.0"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "net.databinder.dispatch" %% "dispatch-core" % "0.10.0",
    "com.oracle.weblogic" % "weblogic-full-client" % "12.1.1",
    "com.typesafe.akka" % "akka-actor_2.10" % "2.1.4"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    resolvers += "Nexus Repository" at "http://vt01ecs02.tb01.test.jse.co.za:9092/nexus/content/groups/public/"
  )

}
