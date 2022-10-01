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
              EmailUtils.sentNewFeedback(requestBody.siteId, requestBody.t,
                requestBody.fullName, requestBody.email, requestBody.content)
              Future.successful("Email with feedback is sent")
          }
        }
        complete(resultF)
      }
    } ~
      path("getFileOrElse") {
        (post & entity(as[String])) { body =>
          logger.info("Getting the request body")
          if (body.contains("email")) {
            val requestJson = JsonMethods.parse(body)
            val requestBody = requestJson.extract[GetFeedbackDataWithEmail]
            Mongo.createFileWithFeedback(requestBody.siteId, requestBody.dateStart,
              requestBody.dateEnd)
            EmailUtils.sentFile(requestBody.email)
            logger.info("File was created and sent")
            complete("File was created and sent")
          } else {
            val resultF = {
              val requestJson = JsonMethods.parse(body)
              val requestBody = requestJson.extract[GetFeedbackData]
              logger.info("Email field not found")
              Mongo.searchDocumentsByDate(requestBody.siteId, requestBody.dateStart,
                requestBody.dateEnd) map { body =>
                  if (body.nonEmpty) {
                    Future.successful(body)
                  } else {
                    Future.successful("Invalid date")
                  }
                }
            }
            complete(resultF)
          }
        }
      } ~ // http://localhost:8088/getFileOrElse/?dateStart=2022-01-34&dateEnd=2022-34-22&siteId=Gachi_nails&email=arcateon@gmail.com
      pathPrefix("getFileOrElse") {
        parameters("dateStart") { dateStart =>
          parameters("dateEnd") { dateEnd =>
            parameters("siteId") { siteId =>
              parameters("email") { email =>
                val resultF = {
                  Mongo.searchDocumentsByDate(siteId, dateStart, dateEnd) map { body =>
                    if (body.nonEmpty) {
                      Mongo.createFileWithFeedback(siteId, dateStart, dateEnd)
                      EmailUtils.sentFile(email)
                      Future.successful("File was created and sent")
                    } else Future.successful("Invalid date")
                  }
                }
                complete(resultF)
              }
            }
          }
        }
      }
  }
}
