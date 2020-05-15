package model.db.dbio

import config.slick.SlickConfig
import dto.entities.{PostDto, TopicDto, UserDto}
import dto.heplers.AddNewTopicRequestIds
import dto.requests.NewTopicRequestDto
import model.db.entities._
import utils.Utils.{generateSecretKey, stringToTimestamp}
import scala.concurrent.ExecutionContext
import config.Config
import validation.failures.TopicOrPostIsNotPresentFailure


trait DBIORepo extends PostsEntity { self: SlickConfig =>

  import driver.api._
  implicit val executionContext: ExecutionContext

  protected def checkIfTopicAndPostExist(topicId: Long, postId: Long): DBIO[Boolean] = posts.filter(x => x.topicId === topicId && x.postId === postId).exists.result

  protected def findUserDbio(externalUser: UserDto): DBIO[Option[UserDto]] =
    users.filter(user => user.nickname === externalUser.nickname && user.email === externalUser.email).result.headOption

  protected def findUserDbio(id: Long): DBIO[Option[UserDto]] = users.filter(_.userId === id).result.headOption

  protected def addUserDbio(newUser: UserDto): DBIO[Long] = (for {
    isUserPresentInDatabase: Option[UserDto] <- findUserDbio(newUser)
    userId: Long <- {
      isUserPresentInDatabase match {
        case Some(user) => DBIO.successful(user.userId.get)
        case None => users.map(user => (user.nickname, user.email)) returning users.map(_.userId) += (newUser.nickname, newUser.email)
      }
    }
  } yield userId).transactionally


  protected def findPostDbio(id: Long): DBIO[Option[PostDto]] = posts.filter(_.postId === id).result.headOption
  protected def addPostDbio(newPost: PostDto): DBIO[Long] =
    posts.map(post => (post.content, post.secretKey, post.postTimestamp, post.userId, post.topicId)) returning posts.map(_.postId) +=
      (newPost.content, newPost.secretKey, newPost.postTimestamp, newPost.userId, newPost.topicId)

  protected def postsPaginationDbio(topicId: Long, postId: Long, nrOfPostsBefore: Long, nrOfPostsAfter: Long): DBIO[Either[Seq[PostDto], TopicOrPostIsNotPresentFailure.type]] = {
    val maxNrOfRequiredPosts = nrOfPostsBefore + nrOfPostsAfter + 1
    val maxNrOfReturnedPosts = Config.appConfig.maxNrOfReturnedPosts

    val (nrOfPostsBeforeChecked, nrOfPostsAfterChecked) = if(maxNrOfRequiredPosts <= maxNrOfReturnedPosts){
      (nrOfPostsBefore, nrOfPostsAfter)
    } else {
      val factor = (nrOfPostsBefore + nrOfPostsAfter).toDouble / (maxNrOfReturnedPosts - 1).toDouble
      ((nrOfPostsBefore.toDouble / factor).floor.toLong, (nrOfPostsAfter.toDouble / factor).floor.toLong)
    }

    checkIfTopicAndPostExist(topicId, postId).flatMap{
      case true => for {
        middlePostTimestamp <- posts.filter(x => x.topicId === topicId && x.postId === postId).map(_.postTimestamp).result.headOption
        requiredTopic = posts.filter(x => x.topicId === topicId)
        posts <- requiredTopic.filter(_.postTimestamp >= middlePostTimestamp).sortBy(_.postTimestamp.desc).take(nrOfPostsAfterChecked + 1).unionAll( // unionAll preserves the order
          requiredTopic.filter(_.postTimestamp < middlePostTimestamp).sortBy(_.postTimestamp.desc).take(nrOfPostsBeforeChecked)).result
      } yield Left(posts)
      case _ => DBIO.successful(Right(TopicOrPostIsNotPresentFailure))
    }.transactionally
  }

  protected def addTopicDbio(newTopic: TopicDto): DBIO[Long] = topics.map(topic => (topic.subject, topic.lastPostTimestamp)) returning topics.map(_.topicId) +=
    (newTopic.subject, newTopic.lastPostTimestamp)

  protected def findTopicDbio(id: Long): DBIO[Option[TopicDto]] = topics.filter(_.topicId === id).result.headOption

  protected def addNewTopicRequestDbio(newTopicRequest: NewTopicRequestDto): DBIO[AddNewTopicRequestIds] = {
    val externalUser = UserDto(newTopicRequest.nickname, newTopicRequest.email)
    val newTopic = TopicDto(newTopicRequest.subject, stringToTimestamp(newTopicRequest.timestamp).get)
    val userDbio = findUserDbio(externalUser)
    val topicIdDbio = addTopicDbio(newTopic)

    (for {
      userOption: Option[UserDto] <- userDbio
      userId <- {
        if(userOption.isEmpty){
          addUserDbio(externalUser)
        } else {
          DBIO.successful(userOption.get.userId.get)
        }
      }
      topicId <- topicIdDbio
      postId <- {
        val newPost = PostDto(newTopicRequest.content, generateSecretKey, stringToTimestamp(newTopicRequest.timestamp).get, userId, topicId)
        addPostDbio(newPost)
      }
    } yield AddNewTopicRequestIds(userId, topicId, postId)).transactionally
  }
}
