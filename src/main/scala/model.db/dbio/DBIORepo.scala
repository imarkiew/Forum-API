package model.db.dbio

import config.slick.SlickConfig
import dto.entities.{PostDto, TopicDto, UserDto}
import dto.heplers.{AddNewPostRequestIds, AddNewTopicRequestIds}
import dto.requests.{NewPostRequestDto, NewTopicRequestDto, UpdatePostRequestDto}
import model.db.entities._
import utils.Utils.{generateSecretKey, stringToTimestamp}
import scala.concurrent.ExecutionContext
import config.Config
import failures.adhoc._


trait DBIORepo extends PostsEntity { self: SlickConfig =>

  import driver.api._
  implicit val executionContext: ExecutionContext

  protected def findUserDBIO(externalUser: UserDto): DBIO[Option[UserDto]] =
    users.filter(user => user.nickname === externalUser.nickname && user.email === externalUser.email).result.headOption

  protected def findUserDBIO(id: Long): DBIO[Option[UserDto]] = users.filter(_.userId === id).result.headOption

  protected def addUserDBIO(newUser: UserDto): DBIO[Long] = (for {
    isUserPresentInDatabase: Option[UserDto] <- findUserDBIO(newUser)
    userId: Long <- {
      isUserPresentInDatabase match {
        case Some(user) => DBIO.successful(user.userId.get)
        case None => users.map(user => (user.nickname, user.email)) returning users.map(_.userId) += (newUser.nickname, newUser.email)
      }
    }
  } yield userId).transactionally

  protected def findPostDBIO(id: Long): DBIO[Option[PostDto]] = posts.filter(_.postId === id).result.headOption
  protected def addPostDBIO(newPost: PostDto): DBIO[Long] =
    posts.map(post => (post.content, post.secretKey, post.postTimestamp, post.userId, post.topicId)) returning posts.map(_.postId) +=
      (newPost.content, newPost.secretKey, newPost.postTimestamp, newPost.userId, newPost.topicId)

  protected def addPostDBIO(newPostRequest: NewPostRequestDto): DBIO[Either[AddNewPostRequestIds, TopicIsNotPresentFailure.type]] = topics.filter(_.topicId === newPostRequest.topicId).result.headOption.flatMap {
    case Some(topic) => for {
      userId <- addUserDBIO(UserDto(newPostRequest.nickname, newPostRequest.email))
      postId <- {
        val newPost = PostDto(newPostRequest.content, generateSecretKey, stringToTimestamp(newPostRequest.timestamp).get, userId, topic.topicId.get)
        addPostDBIO(newPost)
      }
    } yield Left(AddNewPostRequestIds(userId, postId))
    case _ => DBIO.successful(Right(TopicIsNotPresentFailure))
  }.transactionally

  protected def updatePostDBIO(updatePostRequest: UpdatePostRequestDto): DBIO[Either[Int, Failure]] = {
    posts.filter(_.postId === updatePostRequest.postId).result.headOption.flatMap {
      case None => DBIO.successful(PostIsNotPresentFailure)
      case Some(post) => DBIO.successful(post.secretKey == updatePostRequest.secretKey)
    }.flatMap {
      case PostIsNotPresentFailure => DBIO.successful(Right(PostIsNotPresentFailure.apply()))
      case false => DBIO.successful(Right(SecretKeyIsInvalidFailure.apply()))
      case true => posts
        .filter(_.postId === updatePostRequest.postId)
        .map(x => (x.content, x.postTimestamp))
        .update((updatePostRequest.content, stringToTimestamp(updatePostRequest.timestamp).get)).flatMap(x => DBIO.successful(Left(x)))
    }.transactionally
  }

  protected def postsPaginationDBIO(topicId: Long, postId: Long, nrOfPostsBefore: Long, nrOfPostsAfter: Long): DBIO[Either[Seq[PostDto], TopicOrPostIsNotPresentFailure.type]] = {
    val maxNrOfRequiredPosts = nrOfPostsBefore + nrOfPostsAfter + 1
    val maxNrOfReturnedPosts = Config.appConfig.maxNrOfReturnedPosts

    val (nrOfPostsBeforeChecked, nrOfPostsAfterChecked) = if(maxNrOfRequiredPosts <= maxNrOfReturnedPosts){
      (nrOfPostsBefore, nrOfPostsAfter)
    } else {
      val factor = (nrOfPostsBefore + nrOfPostsAfter).toDouble / (maxNrOfReturnedPosts - 1).toDouble
      ((nrOfPostsBefore.toDouble / factor).floor.toLong, (nrOfPostsAfter.toDouble / factor).floor.toLong)
    }

    posts.filter(x => x.topicId === topicId && x.postId === postId).result.headOption.flatMap{
      case Some(_) => for {
        middlePostTimestamp <- posts.filter(x => x.topicId === topicId && x.postId === postId).map(_.postTimestamp).result.headOption
        requiredTopic = posts.filter(x => x.topicId === topicId)
        posts <- requiredTopic.filter(_.postTimestamp >= middlePostTimestamp).sortBy(_.postTimestamp.desc).take(nrOfPostsAfterChecked + 1).unionAll( // unionAll preserves the order
          requiredTopic.filter(_.postTimestamp < middlePostTimestamp).sortBy(_.postTimestamp.desc).take(nrOfPostsBeforeChecked)).result
      } yield Left(posts)
      case _ => DBIO.successful(Right(TopicOrPostIsNotPresentFailure))
    }.transactionally
  }

  protected def addTopicDBIO(newTopic: TopicDto): DBIO[Long] = topics.map(topic => (topic.subject, topic.lastPostTimestamp)) returning topics.map(_.topicId) +=
    (newTopic.subject, newTopic.lastPostTimestamp)

  protected def findTopicDBIO(id: Long): DBIO[Option[TopicDto]] = topics.filter(_.topicId === id).result.headOption

  protected def addTopicDBIO(newTopicRequest: NewTopicRequestDto): DBIO[AddNewTopicRequestIds] = {
    val externalUser = UserDto(newTopicRequest.nickname, newTopicRequest.email)
    val newTopic = TopicDto(newTopicRequest.subject, stringToTimestamp(newTopicRequest.timestamp).get)
    val userDbio = findUserDBIO(externalUser)
    val topicIdDbio = addTopicDBIO(newTopic)

    (for {
      userOption: Option[UserDto] <- userDbio
      userId <- {
        if(userOption.isEmpty){
          addUserDBIO(externalUser)
        } else {
          DBIO.successful(userOption.get.userId.get)
        }
      }
      topicId <- topicIdDbio
      postId <- {
        val newPost = PostDto(newTopicRequest.content, generateSecretKey, stringToTimestamp(newTopicRequest.timestamp).get, userId, topicId)
        addPostDBIO(newPost)
      }
    } yield AddNewTopicRequestIds(userId, topicId, postId)).transactionally
  }

  protected def topNTopicsDBIO(offset: Long, limit: Long): DBIO[Seq[TopicDto]] = topics.sortBy(_.lastPostTimestamp.desc).drop(offset).take(limit).result
}
