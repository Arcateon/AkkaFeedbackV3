package controller

import com.typesafe.config.{Config, ConfigFactory}
import controller.Cases.QuestionAnswer
import email_service_library.domain.EmailConfig
import email_service_library.service.MailSmtpConnector

object EmailUtils {

  val conf: Config = ConfigFactory.
    load("application.conf").
    getConfig("emailConf")

  val emailConfig: EmailConfig = EmailConfig(
    email = conf.getString("sender"),
    protocolName = "default",
    pwd = conf.getString("pwd"),
    smtpSender = conf.getString("sender"),
    smtpHost = conf.getString("smtpHost"),
    imapHost = "none",
    smtpPort = conf.getString("smtpPort"),
    imapPort = "none",
    outlookUser = "none",
    clientId = "none",
    tenantId = "none",
  )
  val connector: MailSmtpConnector = MailSmtpConnector(emailConfig)

  def sentNewFeedback(siteId: String, t: String, fullname: String,
                      email: String, contentList: List[QuestionAnswer]): Unit = {

    val questionAnswer = contentList.map(x => "<ul>" +
      "<li style=\"padding-bottom: 6px;\">question: " + x.question + "</li>" +
      "<li>answer: " + x.answer + "</li>" +
      "</ul>").mkString("\n")

    val content: String =
      "<html lang=\"en\">" +
        "<head>" +
        "<meta charset=\"UTF-8\">" +
        "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">" +
        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
        "<title>Document</title>" +
        "</head>" +
        "<body>" +
        "<div class=\"wrapper\">" +
        "<div class=\"main-body\"" +
        "style=\"background-color: rgba(255, 158, 210, 0.772);" +
        "color: black;" +
        "max-width: 100%;" +
        "margin: 0 auto;" +
        "font-family: Arial, Helvetica, sans-serif;" +
        "padding: 18px;" +
        "border: 3px solid black;" +
        "border-radius: 8px;" +
        "font-size: 20px;\"><div class=\"header\" style=\"border-bottom: 2px solid black;\"><ul>" +
        "<li style=\"padding-bottom: 6px;\">SiteId: " + siteId + "</li>" +
        "<li style=\"padding-bottom: 6px;\">t: " + t + "</li>" +
        "<li style=\"padding-bottom: 6px;\">fullname: " + fullname + "</li>" +
        "<li style=\"padding-bottom: 6px;\">callBack: " + email + "</li>" +
        "</ul>" +
        "</div>" +
        "<div class=\"content\">" +
        questionAnswer +
        "</div></div></div></body></html>"


    val sendTo = Seq(email)
    val sendCc: Seq[String] = Seq.empty
    val sendBcc: Seq[String] = Seq.empty
    val subject = "New feedback"
    val text = content
    val sendFrom = Option("server")
    val attachmentFromPath: Set[String] = Set.empty
    val contentTypeMsg = "text/html; charset=UTF8"

    connector.sendMessage(sendTo, sendCc, sendBcc, subject, text, sendFrom,
      attachmentFromPath, contentTypeMsg)
  }

  def sentFile(email: String): Unit = {

    val sentTo = Seq(email)
    val sendCc: Seq[String] = Seq.empty
    val sendBcc: Seq[String] = Seq.empty
    val subject = "New feedback"
    val text = "File as an attachment"
    val sendFrom = Option("server")
    val attachmentFromPath: Set[String] = Set("feedbackData.csv")
    val contentTypeMsg = "text/html; charset=UTF8"

    connector.sendMessage(sentTo, sendCc, sendBcc, subject, text, sendFrom,
      attachmentFromPath, contentTypeMsg)
  }

}
