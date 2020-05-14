import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import cats.data.NonEmptyList
import cats.data.Validated.{Invalid, Valid}
import dto.requests.NewTopicRequestDto
import json.converter.JsonConverter
import model.db.impl.DBAPI
import validation.failures.NegativeParametersFailure
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}


trait HttpService extends JsonConverter {

  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  val dbApi: DBAPI

  val routes = path("addNewTopic") {
    (post & entity(as[NewTopicRequestDto])) { newTopicRequest =>
      newTopicRequest.validate match {
        case Valid(validNewTopicRequest) => onComplete(dbApi.addNewTopicRequest(validNewTopicRequest)) {
          case Success(ids) => complete(ids)
          case Failure(_) => complete(StatusCodes.InternalServerError)
        }
        case Invalid(validationFailures @ NonEmptyList(_, _)) => complete(StatusCodes.BadRequest, validationFailures.toList)
      }
    }
  } ~
  path("pagination") {
    (get & parameter('topicId.as[Long], 'postId.as[Long], 'nrOfPostsBefore.as[Long], 'nrOfPostsAfter.as[Long])) { (topicId, postId, nrOfPostsBefore, nrOfPostsAfter) =>
      if(nrOfPostsBefore >= 0 && nrOfPostsAfter >= 0) {
        onComplete(dbApi.postPagination(topicId, postId, nrOfPostsBefore, nrOfPostsAfter)) {
          case Success(posts) => complete(posts)
          case Failure(_) => complete(StatusCodes.InternalServerError)
        }
      } else {
        complete(StatusCodes.BadRequest, NegativeParametersFailure.apply())
      }
    }
  }
}
