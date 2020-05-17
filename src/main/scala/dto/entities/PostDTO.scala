package dto.entities

import java.time.Instant


case class PostDTO(content: String, secretKey: String, postTimestamp: Instant, userId: Long, topicId: Long, postId: Option[Long] = None)