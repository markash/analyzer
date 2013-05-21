// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Nexus Releases" at "http://vt01ecs02.tb01.test.jse.co.za:9092/nexus/content/groups/public/"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"



// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.1.1")