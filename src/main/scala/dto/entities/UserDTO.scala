package dto.entities

case class UserDTO(nickname: String, email: String, userId: Option[Long] = None)
