package controller


import com.typesafe.config._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.{ConnectionString, MongoClient, MongoClientSettings, MongoCollection, MongoDatabase}
import com.mongodb.{ServerApi, ServerApiVersion}
import controller.Cases._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.json4s.Formats
import org.json4s.jackson.Serialization
import org.mongodb.scala.model.Filters._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.io.PrintWriter

object Mongo {

  val conf: Config = ConfigFactory.
    load("application.conf").
    getConfig("mongoConf")
  implicit val formats: Formats = org.json4s.DefaultFormats
    .withLong.withDouble.withStrictOptionParsing

  private val uri: String = conf.getString("url")
  private val login: String = conf.getString("login")
  private val pass: String = conf.getString("pass")
  private val db: String = conf.getString("dataBase")

  private val connectionString = s"mongodb://$login:$pass/?authSource=$db"

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


}
