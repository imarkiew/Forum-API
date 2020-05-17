package model.db.impl

import config.slick.SlickConfig
import dto.entities.{PostDTO, TopicDTO, UserDTO}
import dto.heplers.{AddNewPostRequestIds, AddNewTopicRequestIds}
import dto.requests.{AddNewPostRequestDTO, AddNewTopicRequestDTO, DeletePostRequestDTO, UpdatePostRequestDTO}
import failures.adhoc.{Failure, PostIsNotPresentFailure, TopicIsNotPresentFailure, TopicOrPostIsNotPresentFailure}
import model.db.dbio.DBIORepo
import slick.dbio.DBIO
import scala.concurrent.{ExecutionContext, Future}


trait DBAPI extends DBIORepo { self: SlickConfig =>

  override implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit protected def run[T](query: DBIO[T]): Future[T] = db.run(query)

  def findUser(externalUser: UserDTO): Future[Option[UserDTO]] = findUserDBIO(externalUser)
  def findUser(id: Long): Future[Option[UserDTO]] = findUserDBIO(id)
  def findPost(id: Long): Future[Option[PostDTO]] = findPostDBIO(id)
  def addPost(newPostRequest: AddNewPostRequestDTO): Future[Either[AddNewPostRequestIds, TopicIsNotPresentFailure.type]] = addPostDBIO(newPostRequest)
  def deletePost(deletePostRequestDto: DeletePostRequestDTO): Future[Either[Int, Failure]] = deletePostDBIO(deletePostRequestDto)
  def updatePost(updatePostRequestDto: UpdatePostRequestDTO): Future[Either[Int, Failure]] = updatePostDBIO(updatePostRequestDto)
  def postPagination(topicId: Long, postId: Long, nrOfPostsBefore: Long, nrOfPostsAfter: Long): Future[Either[Seq[PostDTO], TopicOrPostIsNotPresentFailure.type]] =
    postsPaginationDBIO(topicId, postId, nrOfPostsBefore, nrOfPostsAfter)

  def findTopic(id: Long): Future[Option[TopicDTO]] = findTopicDBIO(id)
  def addTopic(newTopicRequest: AddNewTopicRequestDTO): Future[AddNewTopicRequestIds] = addTopicDBIO(newTopicRequest)
  def topNTopics(offset: Long, limit: Long): Future[Seq[TopicDTO]] = topNTopicsDBIO(offset, limit)
}
