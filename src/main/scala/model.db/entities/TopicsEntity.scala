package model.db.entities

import java.time.Instant
import config.slick.SlickConfig
import dto.entities.TopicDTO


trait TopicsEntity { self: SlickConfig =>

  import driver.api._

  protected val topics = TableQuery[TopicsTable]

  protected class TopicsTable(tag: Tag) extends Table[TopicDTO](tag, "topics") {
    def topicId = column[Long]("topic_id", O.PrimaryKey, O.AutoInc)
    def subject = column[String]("subject")
    def lastPostTimestamp = column[Instant]("last_post_timestamp")

    def * = (subject, lastPostTimestamp, topicId.?) <> (TopicDTO.tupled, TopicDTO.unapply)
  }
}
