package model.db.entities

import java.time.Instant
import config.slick.SlickConfig
import dto.entities.PostDTO


trait PostsEntity extends UsersEntity with TopicsEntity { self: SlickConfig =>

  import driver.api._

  protected val posts = TableQuery[PostsTable]

  protected class PostsTable(tag: Tag) extends Table[PostDTO](tag, "posts") {

    def postId = column[Long]("post_id", O.PrimaryKey, O.AutoInc)
    def content = column[String]("content")
    def secretKey = column[String]("secret_key")
    def postTimestamp = column[Instant]("post_timestamp")
    def userId = column[Long]("user_id_fk")
    def topicId = column[Long]("topic_id_fk")
    def * = (content, secretKey, postTimestamp, userId, topicId, postId.?) <> (PostDTO.tupled, PostDTO.unapply)
    def userIdFk = foreignKey("user_id_fk", userId, users)(_.userId)
    def topicIdFk = foreignKey("topic_id_fk", topicId, topics)(_.topicId)
  }
}
