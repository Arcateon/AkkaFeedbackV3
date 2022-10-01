package controller


import com.github.tototoshi.csv.CSVWriter
import com.typesafe.config._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.{ConnectionString, MongoClient, MongoClientSettings, MongoCollection, MongoDatabase}
import com.mongodb.{ServerApi, ServerApiVersion}
import com.typesafe.scalalogging.Logger
import controller.Cases._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.json4s.Formats
import org.json4s.jackson.Serialization
import org.mongodb.scala.model.Filters._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.io.File

object Mongo {

  val conf: Config = ConfigFactory.
    load("application.conf").
    getConfig("mongoConf")
  implicit val formats: Formats = org.json4s.DefaultFormats
    .withLong.withDouble.withStrictOptionParsing
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  private val uri: String = conf.getString("url")
//  private val login: String = conf.getString("login")
//  private val pass: String = conf.getString("pass")
  private val db: String = conf.getString("dataBase")

//  private val connectionString = s"mongodb://$login:$pass/?authSource=$db"

  private val mongoClientSettings = MongoClientSettings.builder()
    .applyConnectionString(ConnectionString(uri))
    .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build())
    .build()

  private val mongoClient = MongoClient(mongoClientSettings)
  private val dataBase: MongoDatabase = mongoClient.getDatabase(db)

  private val codecProviderQuestionAnswer: CodecProvider = Macros
    .createCodecProviderIgnoreNone[QuestionAnswer]()
  private val codecProvider: CodecProvider = Macros
    .createCodecProviderIgnoreNone[InputDataWithDate]()
  private val codecRegistry: CodecRegistry =
    fromRegistries(fromProviders(codecProvider, codecProviderQuestionAnswer)
      , DEFAULT_CODEC_REGISTRY)

  private val feedback: MongoCollection[InputDataWithDate] = dataBase
    .withCodecRegistry(codecRegistry)
    .getCollection[InputDataWithDate]("feedback")


  def insertFeedbackData(siteId: String, date: String, email: String, t: String,
                         fullName: String, content: List[QuestionAnswer]): Future[Any] = {

    val document = InputDataWithDate(siteId: String, date: String, email: String, t: String,
      fullName: String, content: List[QuestionAnswer])
    feedback.insertOne(document).toFuture()
  }

  def createFileWithFeedback(siteId: String, dateStart: String, dateEnd: String): Unit = {

    val documentsFuture = feedback.find(and(lt("date", dateEnd),
      gt("date", dateStart), equal("siteId", siteId))).toFuture()

    documentsFuture map { documents =>
      documents map { doc =>
        val questionAnswer = doc.content.map (x =>
          s"${x.question}, ${x.answer}"
          ).mkString(", ")
        val file = new File("feedbackData.csv")
        val writer = CSVWriter.open(file)

        writer.writeAll(List(List("siteId", "date", "email", "t", "fullname", "question - answer")))
        writer.close()

        val newWriter = CSVWriter.open("feedbackData.csv", append = true)
        newWriter.writeRow(List(doc.siteId, doc.date, doc.callback,
          doc.t, doc.fullName, questionAnswer))
        writer.close()
        }
      } recover {
      case _: Exception =>
        logger.info("Invalid date")
    }
    }

  def searchDocumentsByDate(siteId: String, dateStart: String, dateEnd: String): Future[String] = {

    val documentsFuture = feedback.find(and(lt("date", dateEnd),
      gt("date", dateStart), equal("siteId", siteId))).toFuture()

    documentsFuture map { documents =>
      val result: Seq[String] = documents map { doc =>
        Serialization.write(InputDataWithDate(
          doc.siteId, doc.date, doc.callback, doc.t, doc.fullName, doc.content
        ))
      }
      result.mkString("\n")
    } recoverWith {
      case _: Exception =>
        logger.info("Invalid date")
        Future.successful("Invalid date")
    }
  }

}

