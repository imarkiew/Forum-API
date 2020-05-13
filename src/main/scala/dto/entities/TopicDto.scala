package dto.entities

import java.time.Instant


case class TopicDto(subject: String, lastPostTimestamp: Instant, topicId: Option[Long] = None)
