package model.db.entities

import java.sql.Timestamp
import config.slick.SlickConfig
import dto.entities.TopicDto


trait TopicsEntity { self: SlickConfig =>

  import driver.api._

  protected val topics = TableQuery[TopicsTable]

  protected class TopicsTable(tag: Tag) extends Table[TopicDto](tag, "topics") {
    def topicId = column[Long]("topic_id", O.PrimaryKey, O.AutoInc)
    def subject = column[String]("subject")
    def lastPostTimestamp = column[Timestamp]("last_post_timestamp")

    def * = (subject, lastPostTimestamp, topicId.?) <> (TopicDto.tupled, TopicDto.unapply)
  }
}
