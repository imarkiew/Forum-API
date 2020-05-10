package config.slick

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile


trait SlickConfig {
  val driver: JdbcProfile
  val db: Database
}
