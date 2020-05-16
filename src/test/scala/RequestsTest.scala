import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.StatusCodes._
import dto.entities._
import dto.heplers.{AddNewPostRequestIds, AddNewTopicRequestIds}
import dto.requests.{NewPostRequestDto, NewTopicRequestDto, UpdatePostRequestDto}
import h2.H2DB
import json.converter.JsonConverter
import model.db.impl.H2DBImpl
import utils.Utils.stringToTimestamp
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpEntity
import failures.validation.ValidationFailures._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.concurrent.ScalaFutures
import validation.ValidationFailure
import failures.adhoc._


class RequestsTest extends AnyWordSpec
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with ScalatestRouteTest
  with HttpService
  with JsonConverter {

  override val dbApi = H2DBImpl
  override def beforeEach() = H2DB.resetDB
  override def afterAll() = H2DB.shutdownDB

  "The Forum-API" should {

    val topNTopicsRequestString = (offset: String, limit: String) => s"/topNTopics?&offset=$offset&limit=$limit"
    val topic1 = TopicDto("subject_1", stringToTimestamp("2020-05-14T16:23:45.456Z").get, Some(1))
    val topic2 = TopicDto("subject_3", stringToTimestamp("2019-11-03T10:10:34.122Z").get, Some(3))
    val topic3 = TopicDto("subject_2", stringToTimestamp("2019-03-27T08:15:52.194Z").get, Some(2))
    val topic4 = TopicDto("subject_4", stringToTimestamp("2018-08-09T22:04:07.677Z").get, Some(4))

    """
      |for a valid GET request to the topNTopics path
      |   1. return a sequence of the most recent topics which is sorted by last post timestamp in json format
    """.stripMargin in {

      Get(topNTopicsRequestString("0", "3")) ~> routes ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        responseAs[Seq[TopicDto]] shouldBe Seq(topic1, topic2, topic3)
      }

      Get(topNTopicsRequestString("1", "2")) ~> routes ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        responseAs[Seq[TopicDto]] shouldBe Seq(topic2, topic3)
      }

      Get(topNTopicsRequestString("1", "10")) ~> routes ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        responseAs[Seq[TopicDto]] shouldBe Seq(topic2, topic3, topic4)
      }
    }

    """
      |for an invalid GET request with negative parameters to the topNTopics path
      |   1. return the NegativeParametersFailure in json format
    """.stripMargin in {

      Get(topNTopicsRequestString("-2", "1")) ~> routes ~> check {
        status shouldBe BadRequest
        contentType shouldBe `application/json`
        responseAs[NegativeParametersFailure] shouldBe NegativeParametersFailure.apply()
      }
    }
  }

  "The Forum-API" should {

    val paginationRequestString = (topicId: String, postId: String, nrOfPostsBefore: String, nrOfPostsAfter: String) => s"/pagination?topicId=$topicId&postId=$postId&nrOfPostsBefore=$nrOfPostsBefore&nrOfPostsAfter=$nrOfPostsAfter"
    val post1 = PostDto("content_7", "secret_key_7", stringToTimestamp("2020-05-14T16:23:45.456Z").get, 1, 1, Some(7))
    val post2 = PostDto("content_4", "secret_key_4", stringToTimestamp("2020-05-09T22:00:00.103Z").get, 1, 1, Some(4))
    val post3 = PostDto("content_1", "secret_key_1", stringToTimestamp("2020-04-22T19:10:25.474Z").get, 1, 1, Some(1))
    val post4 = PostDto("content_6", "secret_key_6", stringToTimestamp("2020-03-27T08:15:52.004Z").get, 2, 1, Some(6))
    val post5 = PostDto("content_2", "secret_key_2", stringToTimestamp("2020-03-22T11:03:33.532Z").get, 2, 1, Some(2))
    val post6 = PostDto("content_8", "secret_key_8", stringToTimestamp("2019-02-06T17:02:29.000Z").get, 2, 1, Some(8))
    val post7 = PostDto("content_5", "secret_key_5", stringToTimestamp("2018-10-08T14:28:54.374Z").get, 2, 1, Some(5))

    """
      |for a valid GET request to the pagination path
      |   1. return a sequence of posts which is sorted by timestamp around specified post in json format
    """.stripMargin in {

      Get(paginationRequestString("1", "1", "4", "2")) ~> routes ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        responseAs[Seq[PostDto]] shouldBe Seq(post1, post2, post3, post4, post5, post6, post7)
      }
    }

    """
      |for a valid GET request to the pagination path, if the maxNrOfReturnedPosts is exceeded
      |   1. return a sequence of posts which is sorted by timestamp around specified post and proportionally cut in json format
    """.stripMargin in {

      Get(paginationRequestString("1", "1", "4", "3")) ~> routes ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        responseAs[Seq[PostDto]] shouldBe Seq(post1, post2, post3, post4, post5, post6)
      }
    }

    """
      |for an invalid GET request with negative parameters to the pagination path
      |   1. return the NegativeParametersFailure in json format
    """.stripMargin in {

      Get(paginationRequestString("1", "1", "-4", "2")) ~> routes ~> check {
        status shouldBe BadRequest
        contentType shouldBe `application/json`
        responseAs[NegativeParametersFailure] shouldBe NegativeParametersFailure.apply()
      }
    }

    """
      |for a valid GET request to the pagination path if a topic or post is not found
      |   1. return the TopicOrPostIsNotPresentFailure in json format
    """.stripMargin in {

      Get(paginationRequestString("5", "1", "4", "2")) ~> routes ~> check {
        status shouldBe NotFound
        contentType shouldBe `application/json`
        responseAs[TopicOrPostIsNotPresentFailure] shouldBe TopicOrPostIsNotPresentFailure.apply()
      }

      Get(paginationRequestString("1", "12", "4", "2")) ~> routes ~> check {
        status shouldBe NotFound
        contentType shouldBe `application/json`
        responseAs[TopicOrPostIsNotPresentFailure] shouldBe TopicOrPostIsNotPresentFailure.apply()
      }
    }
  }

  "The Forum-API" should {
    """
      |for a valid POST request to the addNewTopic path:
      |   1. add a user if one doesn't exist in the database
      |   2. add a new topic to the database
      |   3. add a new post to the database
      |   4. return ids for a user, topic and post in json format
    """.stripMargin in {

      val nickname = "Paweł"
      val email = "pawel123@gmail.com"
      val subject = "Random topic"
      val content = "Random content"
      val timestamp = "2019-03-11T08:23:41.754Z"
      val newTopic = NewTopicRequestDto(nickname, email, subject, content, timestamp)

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
            val timestampAsTimestamp = stringToTimestamp(timestamp).get
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

    """
      |for a valid POST request to the addNewTopic path:
      |   1. do not add a user if it already exists in the database (only return its id)
      |   2. add a new topic to the database
      |   3. add a new post to the database
      |   4. return ids for a user, topic and post in json format
    """.stripMargin in {

      val nickname = "nick_2"
      val email = "nick2@gmail.com"
      val subject = "Random topic"
      val content = "Random content"
      val timestamp = "2019-03-11T08:23:41.754Z"
      val newTopic = NewTopicRequestDto(nickname, email, subject, content, timestamp)

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
            val timestampAsTimestamp = stringToTimestamp(timestamp).get
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

    """
      |for an invalid POST request to the addNewTopic path
      |   1. send back to the client, a list of validation errors in json format
    """.stripMargin in {

      val nickname = "Paweł"
      val email = "pawel123gmail.com"
      val subject = ""
      val content = "Random content"
      val timestamp = "2019-03-11T08:23:41.754Z"
      val newTopic = NewTopicRequestDto(nickname, email, subject, content, timestamp)

      Post("/addNewTopic", HttpEntity(`application/json`, marshal(newTopic).data.utf8String)) ~> routes ~> check {
        status shouldBe BadRequest
        contentType shouldBe `application/json`
        responseAs[List[ValidationFailure]].map(_.validationError) shouldBe List(InvalidEmailAddress.validationError, InvalidSubjectLength.validationError)
      }
    }
  }

  "The Forum-API" should {
    """
      |for a valid POST request to the addNewPost path
      |   1. add a new user if it does not exist
      |   2. add a new post
      |   3. send back to client a userId and postId in json format
    """.stripMargin in {

      val newPost = NewPostRequestDto("Pjoter007", "piotrw11u@wp.pl", "No za grosz szacunku \n Czego oni ich tam uczą", 2L, "2020-05-16T14:32:10.062Z")

      Post("/addNewPost", HttpEntity(`application/json`, marshal(newPost).data.utf8String)) ~> routes ~> check {
        status shouldBe OK
        contentType shouldBe `application/json`
        responseAs[AddNewPostRequestIds] shouldBe AddNewPostRequestIds(3L, 11L)
      }
    }

      """
        |for a valid POST request to the addNewPost path
        |   1. do not add a new user if it exists
        |   2. add a new post
        |   3. send back to the client a userId and postId in json format
      """.stripMargin in {

        val newPost = NewPostRequestDto("nick_1", "nick1@gmail.com", "New post content", 2L, "2020-05-16T14:32:10.062Z")

        Post("/addNewPost", HttpEntity(`application/json`, marshal(newPost).data.utf8String)) ~> routes ~> check {
          status shouldBe OK
          contentType shouldBe `application/json`
          responseAs[AddNewPostRequestIds] shouldBe AddNewPostRequestIds(1L, 11L)
        }
      }

      """
        |for a valid POST request to the addNewPost path if a topic is not found
        |   1. send back to the client the TopicIsNotPresentFailure in json format
      """.stripMargin in {

        val newPost = NewPostRequestDto("nick_1", "nick1@gmail.com", "New post content", 10L, "2020-05-16T14:32:10.062Z")

        Post("/addNewPost", HttpEntity(`application/json`, marshal(newPost).data.utf8String)) ~> routes ~> check {
          status shouldBe NotFound
          contentType shouldBe `application/json`
          responseAs[TopicIsNotPresentFailure] shouldBe TopicIsNotPresentFailure.apply()
        }
      }

      """
        |for a invalid POST request to the addNewPost path
        |   1. send back the to client a list of validation errors in json format
      """.stripMargin in {

        val newPost = NewPostRequestDto("nick_1", "nick1gmail.com", "", -1L, "2020-05-16T14:32:10:062Z")

        Post("/addNewPost", HttpEntity(`application/json`, marshal(newPost).data.utf8String)) ~> routes ~> check {
          status shouldBe BadRequest
          contentType shouldBe `application/json`
          responseAs[List[ValidationFailure]].map(_.validationError) shouldBe
            List(InvalidEmailAddress.validationError, InvalidContentLength.validationError, InvalidNegativeId.validationError, InvalidTimestamp.validationError)
      }
    }
  }

  "The Forum-API" should {
    """
      |for a valid PATCH request to the updatePost path
      |   1. return the OK status
    """.stripMargin in {

      val partialUpdate = UpdatePostRequestDto("updated_content", 3, "secret_key_3", "2020-01-01T01:07:00.007Z")

      Patch("/updatePost", HttpEntity(`application/json`, marshal(partialUpdate).data.utf8String)) ~> routes ~> check(status shouldBe OK)
    }

    """
      |for a valid PATCH request to the updatePost path if the post is not found
      |   1. return to the client the PostIsNotPresentFailure in json format
    """.stripMargin in {

      val partialUpdate = UpdatePostRequestDto("updated_content", 15, "secret_key_3", "2020-01-01T01:07:00.007Z")

      Patch("/updatePost", HttpEntity(`application/json`, marshal(partialUpdate).data.utf8String)) ~> routes ~> check {
        status shouldBe NotFound
        contentType shouldBe `application/json`
        responseAs[PostIsNotPresentFailure] shouldBe PostIsNotPresentFailure.apply()
      }
    }

    """
      |for a valid PATCH request to the updatePost path if the secretKey is invalid
      |   1. return to the client the SecretKeyIsInvalidFailure in json format
    """.stripMargin in {

      val partialUpdate = UpdatePostRequestDto("updated_content", 3, "xxx", "2020-01-01T01:07:00.007Z")

      Patch("/updatePost", HttpEntity(`application/json`, marshal(partialUpdate).data.utf8String)) ~> routes ~> check {
        status shouldBe BadRequest
        contentType shouldBe `application/json`
        responseAs[SecretKeyIsInvalidFailure] shouldBe SecretKeyIsInvalidFailure.apply()
      }
    }

    """
      |for a invalid PATCH request to the updatePost path
      |   1. return to the client a list of errors in json format in
    """.stripMargin in {

      val partialUpdate = UpdatePostRequestDto("", 3, "secret_key_3", "01-01T01:07:00.007Z")

      Patch("/updatePost", HttpEntity(`application/json`, marshal(partialUpdate).data.utf8String)) ~> routes ~> check {
        status shouldBe BadRequest
        contentType shouldBe `application/json`
        responseAs[List[ValidationFailure]].map(_.validationError) shouldBe List(InvalidContentLength.validationError, InvalidTimestamp.validationError)
      }
    }
  }
}
