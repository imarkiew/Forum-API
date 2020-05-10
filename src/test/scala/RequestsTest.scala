import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.StatusCodes._
import dto.entities._
import dto.heplers.AddNewTopicRequestIds
import dto.requests.NewTopicRequestDto
import h2.H2DB
import json.converter.JsonConverter
import model.db.impl.H2DBImpl
import utils.Utils.stringToTimestamp
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpEntity
import dto.requests.NewTopicRequestDto.{InvalidEmailAddress, InvalidSubjectLength}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import validation.ValidationFailure


class RequestsTest extends AnyWordSpec
  with Matchers
  with BeforeAndAfterAll
  with ScalatestRouteTest
  with HttpService
  with JsonConverter {

  override val dbApi = H2DBImpl
  override def beforeAll() = H2DB.initDB
  override def afterAll() = H2DB.shutdownDB

  "The Forum-API" should {
    """
      |for a valid POST request to the addNewTopic path:
      |   1. add user if one doesn't exist in the database
      |   2. add new topic to the database
      |   3. add new post to the database
      |   4. return OK status
      |   5. return ids for user, topic and post in json format
    """.stripMargin in {

      val nickname = "Paweł"
      val email = "pawel123@gmail.com"
      val subject = "Random topic"
      val content = "Random content"
      val timestamp = "2019-03-11T08:23:41.754Z"
      val newTopic = NewTopicRequestDto(nickname, email, subject , content, timestamp)

      Post("/addNewTopic", HttpEntity(`application/json`, marshal(newTopic).data.utf8String)) ~> routes ~> check {

        status shouldBe OK
        contentType shouldBe `application/json`

        val response = responseAs[AddNewTopicRequestIds]
        val userFuture = dbApi.findUser(response.userId)
        val topicFuture = dbApi.findTopic(response.topicId)
        val postFuture = dbApi.findPost(response.postId)

        val ids = for {
          optionUser <- userFuture
          optionTopic <- topicFuture
          optionPost <- postFuture
        } yield (optionUser, optionTopic, optionPost)

        ScalaFutures.whenReady(ids) {
          case (Some(UserDto(userNickname, userEmail, userId)),
          Some(TopicDto(topicSubject, topicLastPostDateTimestamp, topicId)),
          Some(PostDto(postContent, _, postTimestamp, postUserId, postTopicId, _))) => {
            val timestampAsTimestamp = stringToTimestamp(timestamp)
            userNickname shouldBe nickname
            userEmail shouldBe email
            topicSubject shouldBe subject
            topicLastPostDateTimestamp shouldBe timestampAsTimestamp
            postContent shouldBe content
            postTimestamp shouldBe timestampAsTimestamp
            userId.get shouldBe postUserId
            topicId.get shouldBe postTopicId
          }
          case _ => assert(false);
        }
      }
    }
  }

  "The Forum-API" should {
    """
      |for a valid POST request to the addNewTopic path:
      |   1. do not add user if it already exists in database (only return its id)
      |   2. add new topic to the database
      |   3. add new post to the database
      |   4. return OK status
      |   5. return ids for user, topic and post in json format
    """.stripMargin in {

      val nickname = "nick_2"
      val email = "nick2@gmail.com"
      val subject = "Random topic"
      val content = "Random content"
      val timestamp = "2019-03-11T08:23:41.754Z"
      val newTopic = NewTopicRequestDto(nickname, email, subject , content, timestamp)

      Post("/addNewTopic", HttpEntity(`application/json`, marshal(newTopic).data.utf8String)) ~> routes ~> check {

        status shouldBe OK
        contentType shouldBe `application/json`

        val response = responseAs[AddNewTopicRequestIds]
        val userFuture = dbApi.findUser(response.userId)
        val topicFuture = dbApi.findTopic(response.topicId)
        val postFuture = dbApi.findPost(response.postId)

        val ids = for {
          optionUser <- userFuture
          optionTopic <- topicFuture
          optionPost <- postFuture
        } yield (optionUser, optionTopic, optionPost)

        ScalaFutures.whenReady(ids) {
          case (Some(UserDto(userNickname, userEmail, userId)),
          Some(TopicDto(topicSubject, topicLastPostDateTimestamp, topicId)),
          Some(PostDto(postContent, _, postTimestamp, postUserId, postTopicId, _))) => {
            val timestampAsTimestamp = stringToTimestamp(timestamp)
            userNickname shouldBe nickname
            userEmail shouldBe email
            topicSubject shouldBe subject
            topicLastPostDateTimestamp shouldBe timestampAsTimestamp
            postContent shouldBe content
            postTimestamp shouldBe timestampAsTimestamp
            userId.get shouldBe postUserId
            postUserId shouldBe 2
            topicId.get shouldBe postTopicId
          }
          case _ => assert(false);
        }
      }
    }
  }

  "The Forum-API" should {
    """
      |for a invalid POST request to the addNewTopic path
      |   1. send back to the client, a list of validation errors
    """.stripMargin in {

      val nickname = "Paweł"
      val email = "pawel123gmail.com"
      val subject = ""
      val content = "Random content"
      val timestamp = "2019-03-11T08:23:41.754Z"
      val newTopic = NewTopicRequestDto(nickname, email, subject , content, timestamp)

      Post("/addNewTopic", HttpEntity(`application/json`, marshal(newTopic).data.utf8String)) ~> routes ~> check {
        status shouldBe BadRequest
        contentType shouldBe `application/json`
        responseAs[List[ValidationFailure]].map(_.validationError) shouldBe List(InvalidEmailAddress.validationError, InvalidSubjectLength.validationError)
      }
    }
  }
}
