package json.converter

import spray.json.{JsValue, _}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import dto.entities.{PostDto, TopicDto}
import dto.requests.{NewPostRequestDto, NewTopicRequestDto, UpdatePostRequestDto}
import dto.heplers.{AddNewPostRequestIds, AddNewTopicRequestIds}
import validation.ValidationFailure
import failures.adhoc._
import scala.util.Try
import utils.Utils.stringToTimestamp


trait JsonConverter extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val newTopicRequestJsonFormat: RootJsonFormat[NewTopicRequestDto] = jsonFormat5(NewTopicRequestDto.apply)
  implicit val newTopicRequestResponseJsonFormat: RootJsonFormat[AddNewTopicRequestIds] = jsonFormat3(AddNewTopicRequestIds)
  implicit val negativeParametersValidationFailureJsonFormat: RootJsonFormat[NegativeParametersFailure] = jsonFormat1(NegativeParametersFailure)
  implicit val topicOrPostIsNotPresentFailureJsonFormat: RootJsonFormat[TopicOrPostIsNotPresentFailure] = jsonFormat1(TopicOrPostIsNotPresentFailure)
  implicit val newPostRequestDtoJsonFormat: RootJsonFormat[NewPostRequestDto] = jsonFormat5(NewPostRequestDto)
  implicit val newPostRequestResponseJsonFormat: RootJsonFormat[AddNewPostRequestIds] = jsonFormat2(AddNewPostRequestIds)
  implicit val topicIsNotPresentFailureJsonFormat: RootJsonFormat[TopicIsNotPresentFailure] = jsonFormat1(TopicIsNotPresentFailure)
  implicit val updatePostRequestDtoJsonFormat: RootJsonFormat[UpdatePostRequestDto] = jsonFormat4(UpdatePostRequestDto)
  implicit val postIsNotPresentFailureJsonFormat: RootJsonFormat[PostIsNotPresentFailure] = jsonFormat1(PostIsNotPresentFailure)
  implicit val secretKeyIsInvalidFailureJsonFormat: RootJsonFormat[SecretKeyIsInvalidFailure] = jsonFormat1(SecretKeyIsInvalidFailure)

  implicit val topicDtoJsonFormat: RootJsonFormat[TopicDto] = new RootJsonFormat[TopicDto] {
    def write(topicDto: TopicDto): JsValue = JsObject(
      "subject" -> JsString(topicDto.subject),
      "lastPostTimestamp" -> JsString(topicDto.lastPostTimestamp.toString),
      "topicId" -> JsNumber(topicDto.topicId.get)
    )

    def read(json: JsValue): TopicDto = {
      val fields = json.asJsObject().fields
      TopicDto(
        fields("subject").convertTo[String],
        stringToTimestamp(fields("lastPostTimestamp").convertTo[String]).get,
        fields("topicId").convertTo[Option[Long]]
      )
    }
  }

  implicit val postDtoJsonFormat: RootJsonFormat[PostDto] = new RootJsonFormat[PostDto]  {
    def write(postDto: PostDto): JsValue = JsObject(
      "content" -> JsString(postDto.content),
      "secretKey" -> JsString(postDto.secretKey),
      "postTimestamp" -> JsString(postDto.postTimestamp.toString),
      "userId" -> JsNumber(postDto.userId),
      "topicId" -> JsNumber(postDto.topicId),
      "postId" -> JsNumber(postDto.postId.get)
    )

    def read(json: JsValue): PostDto = {
      val fields = json.asJsObject().fields
      PostDto(
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