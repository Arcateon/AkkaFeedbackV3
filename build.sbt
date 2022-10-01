import Dependencies._

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.17"

lazy val root = (project in file("."))
  .settings(
    name := "AkkaFeedbackV3",
    credentials += commonCredentials,
    resolvers := commonResolvers,
    libraryDependencies ++= Seq(
      akkaHttp,
      akkaStream,
      specs2Core % Test,
      jsonData,
      jacksonData,
      mongoDb,
      log,
      scalaLogging,
      sendEmail
    )
  )

val typeSafeRepo = Resolver.typesafeRepo("releases")
val nexusReleases = ("Nexus Releases" at "https://nexus2.teko.io/repository/teko-releases/")
  .withAllowInsecureProtocol(true)
val nexusSnapshots = ("Nexus Snapshots" at "https://nexus2.teko.io/repository/teko-snapshots/")
  .withAllowInsecureProtocol(true)
val sonatype = Resolver.sonatypeRepo("snapshots")

val nexusSnapshots2 = ("Nexus Snapshots" at "https://nexus2.teko.io/repository/teko-snapshots-nexus2/")
val nexusReleases2 = ("Nexus Releases" at "https://nexus2.teko.io/repository/teko-releases-nexus2/")

def commonResolvers = Seq(typeSafeRepo, sonatype, nexusReleases2, nexusSnapshots2, nexusReleases, nexusSnapshots, Classpaths.typesafeReleases, Resolver.mavenLocal)

def commonCredentials = {
  val credentialsFile = Path.userHome / ".sbt" / ".credentials"

  val exists = credentialsFile.exists()
  if (exists)
    Credentials(credentialsFile)
  else
    Credentials("Sonatype Nexus Repository Manager", "nexus2.teko.io", System.getenv("NEXUS_REPOSITORY_USERNAME"), System.getenv("NEXUS_REPOSITORY_PASSWORD"))
}
