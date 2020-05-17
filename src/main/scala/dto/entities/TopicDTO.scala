package dto.entities

import java.time.Instant


case class TopicDTO(subject: String, lastPostTimestamp: Instant, topicId: Option[Long] = None)
