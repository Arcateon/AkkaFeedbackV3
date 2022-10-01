package controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.json4s.Formats
import org.json4s.jackson._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.typesafe.scalalogging._
import controller.Cases._
import org.slf4j.LoggerFactory

import java.time.LocalDate

object Routes {

  implicit val formats: Formats = org.json4s.DefaultFormats
    .withLong.withDouble.withStrictOptionParsing
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  val route: Route = {
    path("newFeedback") {
      (post & entity(as[String])) { body =>
        logger.info("Getting the request body")
        val requestJson = JsonMethods.parse(body)
        val requestBody = requestJson.extract[InputData]
        val date = LocalDate.now.toString

        val resultF = {
          Mongo.insertFeedbackData(requestBody.siteId,
            date,
            requestBody.email,
            requestBody.t,
            requestBody.fullName,
            requestBody.content) flatMap {
            case _: Exception =>
              logger.info("Adding new feedback is failed")
              Future.successful("failed")
            case _ =>
              logger.info("Adding new feedback into db")
              EmailUtils.sendNewFeedback(requestBody.email)
              Future.successful("Email with feedback is sent")
          }
        }
        complete(resultF)
      }
    }
  }
}
