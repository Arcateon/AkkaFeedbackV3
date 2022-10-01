import sbt._

object Dependencies {
  lazy val akkaVersion = "2.6.19"
  lazy val akkaHttpVersion = "10.2.9"

  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

  lazy val specs2Core = "org.specs2" %% "specs2-core" % "4.16.0"
  lazy val jsonData = "org.json4s" %% "json4s-native" % "4.0.5"
  lazy val jacksonData = "org.json4s" %% "json4s-jackson" % "4.0.5"
  lazy val mongoDb = "org.mongodb.scala" %% "mongo-scala-driver" % "4.6.0"
  lazy val log = "ch.qos.logback" % "logback-classic" % "1.3.1"
  lazy val scalaLogging =  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
  lazy val sendEmail = "com.teko.common" %% "email-service" % "0.0.6"
  lazy val scalaCsv = "com.github.tototoshi" %% "scala-csv" % "1.3.10"
}