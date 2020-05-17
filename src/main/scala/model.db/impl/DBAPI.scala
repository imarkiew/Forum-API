package model.db.impl

import config.slick.SlickConfig
import dto.entities.{PostDto, TopicDto, UserDto}
import dto.heplers.{AddNewPostRequestIds, AddNewTopicRequestIds}
import dto.requests.{AddNewPostRequestDto, AddNewTopicRequestDto, UpdatePostRequestDto}
import failures.adhoc.{Failure, TopicIsNotPresentFailure, TopicOrPostIsNotPresentFailure}
import model.db.dbio.DBIORepo
import slick.dbio.DBIO
import scala.concurrent.{ExecutionContext, Future}


trait DBAPI extends DBIORepo { self: SlickConfig =>

  override implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit protected def run[T](query: DBIO[T]): Future[T] = db.run(query)

  def findUser(externalUser: UserDto): Future[Option[UserDto]] = findUserDBIO(externalUser)
  def findUser(id: Long): Future[Option[UserDto]] = findUserDBIO(id)
  def findPost(id: Long): Future[Option[PostDto]] = findPostDBIO(id)
  def addPost(newPostRequest: AddNewPostRequestDto): Future[Either[AddNewPostRequestIds, TopicIsNotPresentFailure.type]] = addPostDBIO(newPostRequest)
  def updatePost(updatePostRequestDto: UpdatePostRequestDto): Future[Either[Int, Failure]] = updatePostDBIO(updatePostRequestDto)
  def postPagination(topicId: Long, postId: Long, nrOfPostsBefore: Long, nrOfPostsAfter: Long): Future[Either[Seq[PostDto], TopicOrPostIsNotPresentFailure.type]] =
    postsPaginationDBIO(topicId, postId, nrOfPostsBefore, nrOfPostsAfter)

  def findTopic(id: Long): Future[Option[TopicDto]] = findTopicDBIO(id)
  def addTopic(newTopicRequest: AddNewTopicRequestDto): Future[AddNewTopicRequestIds] = addTopicDBIO(newTopicRequest)
  def topNTopics(offset: Long, limit: Long): Future[Seq[TopicDto]] = topNTopicsDBIO(offset, limit)
}
