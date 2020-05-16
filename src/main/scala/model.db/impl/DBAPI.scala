package model.db.impl

import config.slick.SlickConfig
import dto.entities.{PostDto, TopicDto, UserDto}
import dto.heplers.{AddNewPostRequestIds, AddNewTopicRequestIds}
import dto.requests.{NewPostRequestDto, NewTopicRequestDto}
import failures.adhoc.{TopicIsNotPresentFailure, TopicOrPostIsNotPresentFailure}
import model.db.dbio.DBIORepo
import slick.dbio.DBIO
import scala.concurrent.{ExecutionContext, Future}


trait DBAPI extends DBIORepo { self: SlickConfig =>

  override implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit protected def run[T](query: DBIO[T]): Future[T] = db.run(query)

  def findUser(externalUser: UserDto): Future[Option[UserDto]] = findUserDbio(externalUser)
  def findUser(id: Long): Future[Option[UserDto]] = findUserDbio(id)
  def findPost(id: Long): Future[Option[PostDto]] = findPostDbio(id)
  def addPost(newPostRequest: NewPostRequestDto): Future[Either[AddNewPostRequestIds, TopicIsNotPresentFailure.type]] = addPostDbio(newPostRequest)
  def postPagination(topicId: Long, postId: Long, nrOfPostsBefore: Long, nrOfPostsAfter: Long): Future[Either[Seq[PostDto], TopicOrPostIsNotPresentFailure.type]] =
    postsPaginationDbio(topicId, postId, nrOfPostsBefore, nrOfPostsAfter)

  def findTopic(id: Long): Future[Option[TopicDto]] = findTopicDbio(id)
  def addTopic(newTopicRequest: NewTopicRequestDto): Future[AddNewTopicRequestIds] = addTopicDbio(newTopicRequest)
  def topNTopics(offset: Long, limit: Long): Future[Seq[TopicDto]] = topNTopicsDbio(offset, limit)
}
