package dto.entities

import java.sql.Timestamp


case class TopicDto(subject: String, lastPostTimestamp: Timestamp, topicId: Option[Long] = None)
