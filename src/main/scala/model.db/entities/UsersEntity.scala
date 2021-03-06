package model.db.entities

import config.slick.SlickConfig
import dto.entities.UserDTO


trait UsersEntity { self: SlickConfig =>

  import driver.api._

  protected val users = TableQuery[UsersTable]

  protected class UsersTable(tag: Tag) extends Table[UserDTO](tag, "users") {
    def userId = column[Long]("user_id", O.PrimaryKey, O.AutoInc)
    def nickname = column[String]("nickname")
    def email = column[String]("email")

    def * = (nickname, email, userId.?) <> (UserDTO.tupled, UserDTO.unapply)
  }
}
