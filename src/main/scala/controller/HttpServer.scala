package controller

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config._
import org.json4s.Formats

import scala.concurrent.ExecutionContextExecutor

object HttpServer {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val formats: Formats = org.json4s.DefaultFormats
    .withLong.withDouble.withStrictOptionParsing

  val conf: Config = ConfigFactory.
    load("application.conf").
    getConfig("serverConf")

  def main(args: Array[String]): Unit = {

    implicit val log: LoggingAdapter = Logging(system, "main")
    val port = conf.getString("port").toInt
    val bindingFuture =
      Http().bindAndHandle(Routes.route, "localhost", port)
    log.info(s"Server started at the port $port")

  }

}
