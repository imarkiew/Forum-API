package dto.entities

import java.sql.Timestamp


case class PostDto(content: String, secretKey: String, postTimestamp: Timestamp, userId: Long, topicId: Long, postId: Option[Long] = None)