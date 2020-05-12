package dto.entities

case class UserDto(nickname: String, email: String, userId: Option[Long] = None)
