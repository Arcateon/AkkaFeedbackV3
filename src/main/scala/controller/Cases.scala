package controller

object Cases {

  case class QuestionAnswer(question: String, answer: String)

  case class InputData(siteId: String, email: String, t: String,
                       fullName: String, content: List[QuestionAnswer])

  case class InputDataWithDate(siteId: String, date: String, callback: String, t: String,
                               fullName: String, content: List[QuestionAnswer])

}
