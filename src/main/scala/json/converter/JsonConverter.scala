package json.converter

import java.time.Instant
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import dto.entities.PostDto
import dto.requests.NewTopicRequestDto
import dto.heplers.AddNewTopicRequestIds
import validation.ValidationFailure
import validation.failures.NegativeParametersFailure
import scala.util.Try


trait JsonConverter extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val newTopicRequestJsonFormat: RootJsonFormat[NewTopicRequestDto] = jsonFormat5(NewTopicRequestDto.apply)
  implicit val newTopicRequestResponseJsonFormat: RootJsonFormat[AddNewTopicRequestIds] = jsonFormat3(AddNewTopicRequestIds)
  implicit val postDtoJsonFormat: RootJsonFormat[PostDto] = jsonFormat6(PostDto)
  implicit val negativeParametersValidationFailureJsonFormat: RootJsonFormat[NegativeParametersFailure] = jsonFormat1(NegativeParametersFailure)

  // marked as lazy, since there were some initialization problems with Java in Scala app
  implicit lazy val timestampJsFormat: RootJsonFormat[Instant] = new RootJsonFormat[Instant]  {
    def write(timestamp: Instant): JsValue = JsObject("timestamp" -> JsString(timestamp.toString))
    def read(json: JsValue): Instant = {
      json.asJsObject().getFields("timestamp") match {
        case Seq(JsString(timestamp)) => Instant.parse(timestamp)
        case _ => throw new RuntimeException("Exception during parsing json value to timestamp")
      }
    }
  }

  implicit val newTopicRequestValidationFailuresJsonFormat: RootJsonFormat[List[ValidationFailure]] =
    new RootJsonFormat[List[ValidationFailure]] {

    val validationFailuresField = "validation_failures"

    def write(failures: List[ValidationFailure]): JsValue =
      JsObject(
              (validationFailuresField, JsArray(failures.map(x => JsString(x.validationError))))
      )

    def read(value: JsValue): List[ValidationFailure] = {
      Try {
        val jsArray = value
          .asJsObject
          .getFields(validationFailuresField)
          .head match {case JsArray(errors) => errors}

        jsArray
          .toList
          .map(x => new ValidationFailure(x.convertTo[String]))
      }.getOrElse(List())
    }
  }
}