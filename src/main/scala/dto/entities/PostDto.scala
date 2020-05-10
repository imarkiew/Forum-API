package dto.entities

import java.sql.Timestamp


case class PostDto(content: String, secretKey: String, postTimestamp: Timestamp, userId: Int, topicId: Int, postId: Option[Int] = None)