package json.converter

import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import dto.requests.NewTopicRequestDto
import dto.heplers.AddNewTopicRequestIds
import validation.ValidationFailure
import scala.util.Try


trait JsonConverter extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val newTopicRequestJsonFormat = jsonFormat5(NewTopicRequestDto.apply)
  implicit val newTopicRequestResponseJsonFormat = jsonFormat3(AddNewTopicRequestIds)

  implicit object NewTopicRequestValidationFailuresJsonFormat extends RootJsonFormat[List[ValidationFailure]] {

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