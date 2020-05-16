import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
import dto.requests.{NewPostRequestDto, NewTopicRequestDto}
import json.converter.JsonConverter
import model.db.impl.DBAPI
import failures.adhoc.{NegativeParametersFailure, TopicIsNotPresentFailure, TopicOrPostIsNotPresentFailure}
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}


trait HttpService extends JsonConverter {

  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  val dbApi: DBAPI

  val routes = path("addNewTopic") {
    (post & entity(as[NewTopicRequestDto])) { newTopicRequest =>
      newTopicRequest.validate match {
        case Valid(validNewTopicRequest) => onComplete(dbApi.addTopic(validNewTopicRequest)) {
          case Success(ids) => complete(ids)
          case Failure(_) => complete(StatusCodes.InternalServerError)
        }
        case Invalid(validationFailures @ NonEmptyList(_, _)) => complete(StatusCodes.BadRequest, validationFailures.toList)
      }
    }
  } ~
  path("addNewPost") {
    (post & entity(as[NewPostRequestDto])) { newPostRequest =>
      newPostRequest.validate match {
        case Valid(validNewPostRequest) => onComplete(dbApi.addPost(validNewPostRequest)) {
          case Success(eitherIds) => eitherIds match {
            case Left(ids) => complete(ids)
            case Right(TopicIsNotPresentFailure) => complete(StatusCodes.NotFound, TopicIsNotPresentFailure.apply())
          }
          case Failure(_) => complete(StatusCodes.InternalServerError)
        }
        case Invalid(validationFailures @ NonEmptyList(_, _)) => complete(StatusCodes.BadRequest, validationFailures.toList)
      }

    }
  } ~
  path("topNTopics") {
    (get & parameter('offset.as[Long], 'limit.as[Long])) { (offset, limit) =>
      if(offset >= 0 && limit >= 0){
        onComplete(dbApi.topNTopics(offset, limit)) {
          case Success(topics) => complete(topics)
          case Failure(_) => complete(StatusCodes.InternalServerError)
        }
      } else {
        complete(StatusCodes.BadRequest, NegativeParametersFailure.apply())
      }
    }
  } ~
  path("pagination") {
    (get & parameter('topicId.as[Long], 'postId.as[Long], 'nrOfPostsBefore.as[Long], 'nrOfPostsAfter.as[Long])) { (topicId, postId, nrOfPostsBefore, nrOfPostsAfter) =>
      if(nrOfPostsBefore >= 0 && nrOfPostsAfter >= 0) {
        onComplete(dbApi.postPagination(topicId, postId, nrOfPostsBefore, nrOfPostsAfter)) {
          case Success(eitherPosts) => eitherPosts match {
            case Left(posts) => complete(posts)
            case Right(TopicOrPostIsNotPresentFailure) => complete(StatusCodes.NotFound, TopicOrPostIsNotPresentFailure.apply())
          }
          case Failure(_) => complete(StatusCodes.InternalServerError)
        }
      } else {
        complete(StatusCodes.BadRequest, NegativeParametersFailure.apply())
      }
    }
  }
}
