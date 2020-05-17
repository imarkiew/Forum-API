package json.converter

import spray.json.{JsValue, _}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import dto.entities.{PostDTO, TopicDTO}
import dto.requests.{AddNewPostRequestDTO, AddNewTopicRequestDTO, UpdatePostRequestDTO}
import dto.heplers.{AddNewPostRequestIds, AddNewTopicRequestIds}
import validation.ValidationFailure
import failures.adhoc._
import scala.util.Try
import utils.Utils.stringToTimestamp


trait JsonConverter extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val newTopicRequestJsonFormat: RootJsonFormat[AddNewTopicRequestDTO] = jsonFormat5(AddNewTopicRequestDTO.apply)
  implicit val newTopicRequestResponseJsonFormat: RootJsonFormat[AddNewTopicRequestIds] = jsonFormat3(AddNewTopicRequestIds)
  implicit val negativeParametersValidationFailureJsonFormat: RootJsonFormat[NegativeParametersFailure] = jsonFormat1(NegativeParametersFailure)
  implicit val topicOrPostIsNotPresentFailureJsonFormat: RootJsonFormat[TopicOrPostIsNotPresentFailure] = jsonFormat1(TopicOrPostIsNotPresentFailure)
  implicit val newPostRequestDtoJsonFormat: RootJsonFormat[AddNewPostRequestDTO] = jsonFormat5(AddNewPostRequestDTO)
  implicit val newPostRequestResponseJsonFormat: RootJsonFormat[AddNewPostRequestIds] = jsonFormat2(AddNewPostRequestIds)
  implicit val topicIsNotPresentFailureJsonFormat: RootJsonFormat[TopicIsNotPresentFailure] = jsonFormat1(TopicIsNotPresentFailure)
  implicit val updatePostRequestDtoJsonFormat: RootJsonFormat[UpdatePostRequestDTO] = jsonFormat4(UpdatePostRequestDTO)
  implicit val postIsNotPresentFailureJsonFormat: RootJsonFormat[PostIsNotPresentFailure] = jsonFormat1(PostIsNotPresentFailure)
  implicit val secretKeyIsInvalidFailureJsonFormat: RootJsonFormat[SecretKeyIsInvalidFailure] = jsonFormat1(SecretKeyIsInvalidFailure)

  implicit val topicDtoJsonFormat: RootJsonFormat[TopicDTO] = new RootJsonFormat[TopicDTO] {
    def write(topicDto: TopicDTO): JsValue = JsObject(
      "subject" -> JsString(topicDto.subject),
      "lastPostTimestamp" -> JsString(topicDto.lastPostTimestamp.toString),
      "topicId" -> JsNumber(topicDto.topicId.get)
    )

    def read(json: JsValue): TopicDTO = {
      val fields = json.asJsObject().fields
      TopicDTO(
        fields("subject").convertTo[String],
        stringToTimestamp(fields("lastPostTimestamp").convertTo[String]).get,
        fields("topicId").convertTo[Option[Long]]
      )
    }
  }

  implicit val postDtoJsonFormat: RootJsonFormat[PostDTO] = new RootJsonFormat[PostDTO]  {
    def write(postDto: PostDTO): JsValue = JsObject(
      "content" -> JsString(postDto.content),
      "secretKey" -> JsString(postDto.secretKey),
      "postTimestamp" -> JsString(postDto.postTimestamp.toString),
      "userId" -> JsNumber(postDto.userId),
      "topicId" -> JsNumber(postDto.topicId),
      "postId" -> JsNumber(postDto.postId.get)
    )

    def read(json: JsValue): PostDTO = {
      val fields = json.asJsObject().fields
      PostDTO(
        fields("content").convertTo[String],
        fields("secretKey").convertTo[String],
        stringToTimestamp(fields("postTimestamp").convertTo[String]).get,
        fields("userId").convertTo[Long],
        fields("topicId").convertTo[Long],
        fields("postId").convertTo[Option[Long]]
      )
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