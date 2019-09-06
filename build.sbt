name := "Rejection_HTTP"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.23"
libraryDependencies +="com.typesafe.akka" %% "akka-stream" % "2.5.23"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.9"
libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % "2.4.4"
libraryDependencies += "com.typesafe.akka" %% "akka-http-xml" % "10.1.9"



libraryDependencies ++= Seq(
"com.typesafe.play" %% "play-json" % "2.6.7",
"de.heikoseeberger" %% "akka-http-play-json" % "1.20.0"
)
