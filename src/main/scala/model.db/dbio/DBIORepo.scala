package model.db.dbio

import config.slick.SlickConfig
import dto.entities.{PostDTO, TopicDTO, UserDTO}
import dto.heplers.{AddNewPostRequestIds, AddNewTopicRequestIds}
import dto.requests.{AddNewPostRequestDTO, AddNewTopicRequestDTO, DeletePostRequestDTO, UpdatePostRequestDTO}
import model.db.entities._
import utils.Utils.{generateSecretKey, stringToTimestamp}
import scala.concurrent.ExecutionContext
import config.Config
import failures.adhoc._


trait DBIORepo extends PostsEntity { self: SlickConfig =>

  import driver.api._
  implicit val executionContext: ExecutionContext

  protected def findUserDBIO(externalUser: UserDTO): DBIO[Option[UserDTO]] =
    users.filter(user => user.nickname === externalUser.nickname && user.email === externalUser.email).result.headOption

  protected def findUserDBIO(id: Long): DBIO[Option[UserDTO]] = users.filter(_.userId === id).result.headOption

  protected def addUserDBIO(newUser: UserDTO): DBIO[Long] = (for {
    isUserPresentInDatabase: Option[UserDTO] <- findUserDBIO(newUser)
    userId: Long <- {
      isUserPresentInDatabase match {
        case Some(user) => DBIO.successful(user.userId.get)
        case None => users.map(user => (user.nickname, user.email)) returning users.map(_.userId) += (newUser.nickname, newUser.email)
      }
    }
  } yield userId).transactionally

  protected def findPostDBIO(id: Long): DBIO[Option[PostDTO]] = posts.filter(_.postId === id).result.headOption
  protected def addPostDBIO(newPost: PostDTO): DBIO[Long] =
    posts.map(post => (post.content, post.secretKey, post.postTimestamp, post.userId, post.topicId)) returning posts.map(_.postId) +=
      (newPost.content, newPost.secretKey, newPost.postTimestamp, newPost.userId, newPost.topicId)

  protected def addPostDBIO(newPostRequest: AddNewPostRequestDTO): DBIO[Either[AddNewPostRequestIds, TopicIsNotPresentFailure.type]] = topics.filter(_.topicId === newPostRequest.topicId).result.headOption.flatMap {
    case Some(topic) => for {
      userId <- addUserDBIO(UserDTO(newPostRequest.nickname, newPostRequest.email))
      postId <- {
        val newPost = PostDTO(newPostRequest.content, generateSecretKey, stringToTimestamp(newPostRequest.timestamp).get, userId, topic.topicId.get)
        addPostDBIO(newPost)
      }
    } yield Left(AddNewPostRequestIds(userId, postId))
    case _ => DBIO.successful(Right(TopicIsNotPresentFailure))
  }.transactionally

  protected def updatePostDBIO(updatePostRequest: UpdatePostRequestDTO): DBIO[Either[Int, Failure]] = {
    posts.filter(_.postId === updatePostRequest.postId).result.headOption.map {
      case None => PostIsNotPresentFailure
      case Some(post) => post.secretKey == updatePostRequest.secretKey
    }.flatMap {
      case PostIsNotPresentFailure => DBIO.successful(Right(PostIsNotPresentFailure.apply()))
      case false => DBIO.successful(Right(SecretKeyIsInvalidFailure.apply()))
      case true => posts
        .filter(_.postId === updatePostRequest.postId)
        .map(x => (x.content, x.postTimestamp))
        .update((updatePostRequest.content, stringToTimestamp(updatePostRequest.timestamp).get)).map(Left(_))
    }.transactionally
  }

  protected def deletePostDBIO(deletePostRequestDto: DeletePostRequestDTO): DBIO[Either[Int, Failure]] = {
    posts.filter(_.postId === deletePostRequestDto.postId).result.headOption.map {
      case None => PostIsNotPresentFailure
      case Some(post) => post.secretKey == deletePostRequestDto.secretKey
    }.flatMap {
      case PostIsNotPresentFailure => DBIO.successful(Right(PostIsNotPresentFailure.apply()))
      case false => DBIO.successful(Right(SecretKeyIsInvalidFailure.apply()))
      case true => posts.filter(_.postId === deletePostRequestDto.postId).delete.map(Left(_))
    }.transactionally
  }

  protected def postsPaginationDBIO(topicId: Long, postId: Long, nrOfPostsBefore: Long, nrOfPostsAfter: Long): DBIO[Either[Seq[PostDTO], TopicOrPostIsNotPresentFailure.type]] = {
    val maxNrOfRequiredPosts = nrOfPostsBefore + nrOfPostsAfter + 1
    val maxNrOfReturnedPosts = Config.appConfig.maxNrOfReturnedPosts

    val (nrOfPostsBeforeChecked, nrOfPostsAfterChecked) = if(maxNrOfRequiredPosts <= maxNrOfReturnedPosts){
      (nrOfPostsBefore, nrOfPostsAfter)
    } else {
      val factor = (nrOfPostsBefore + nrOfPostsAfter).toDouble / (maxNrOfReturnedPosts - 1).toDouble
      ((nrOfPostsBefore.toDouble / factor).floor.toLong, (nrOfPostsAfter.toDouble / factor).floor.toLong)
    }

    posts.filter(x => x.topicId === topicId && x.postId === postId).map(_.postTimestamp).result.headOption.flatMap {
      case Some(middlePostTimestamp) =>
        val requiredTopic = posts.filter(_.topicId === topicId)
        for {
          posts <- requiredTopic.filter(_.postTimestamp >= middlePostTimestamp).sortBy(_.postTimestamp.desc).take(nrOfPostsAfterChecked + 1).unionAll( // unionAll preserves the order
            requiredTopic.filter(_.postTimestamp < middlePostTimestamp).sortBy(_.postTimestamp.desc).take(nrOfPostsBeforeChecked)).result
      } yield Left(posts)
      case _ => DBIO.successful(Right(TopicOrPostIsNotPresentFailure))
    }.transactionally
  }

  protected def addTopicDBIO(newTopic: TopicDTO): DBIO[Long] = topics.map(topic => (topic.subject, topic.lastPostTimestamp)) returning topics.map(_.topicId) +=
    (newTopic.subject, newTopic.lastPostTimestamp)

  protected def findTopicDBIO(id: Long): DBIO[Option[TopicDTO]] = topics.filter(_.topicId === id).result.headOption

  protected def addTopicDBIO(newTopicRequest: AddNewTopicRequestDTO): DBIO[AddNewTopicRequestIds] = {
    val externalUser = UserDTO(newTopicRequest.nickname, newTopicRequest.email)
    val newTopic = TopicDTO(newTopicRequest.subject, stringToTimestamp(newTopicRequest.timestamp).get)
    val userDbio = findUserDBIO(externalUser)
    val topicIdDbio = addTopicDBIO(newTopic)

    (for {
      userOption: Option[UserDTO] <- userDbio
      userId <- {
        if(userOption.isEmpty){
          addUserDBIO(externalUser)
        } else {
          DBIO.successful(userOption.get.userId.get)
        }
      }
      topicId <- topicIdDbio
      postId <- {
        val newPost = PostDTO(newTopicRequest.content, generateSecretKey, stringToTimestamp(newTopicRequest.timestamp).get, userId, topicId)
        addPostDBIO(newPost)
      }
    } yield AddNewTopicRequestIds(userId, topicId, postId)).transactionally
  }

  protected def topNTopicsDBIO(offset: Long, limit: Long): DBIO[Seq[TopicDTO]] = {
    val checkedLimit = if(limit <= Config.appConfig.maxNrOfReturnedTopics) limit else Config.appConfig.maxNrOfReturnedTopics
    topics.sortBy(_.lastPostTimestamp.desc).drop(offset).take(checkedLimit).result
  }
}
