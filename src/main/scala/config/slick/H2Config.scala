package config.slick

import slick.jdbc.JdbcBackend.Database

trait H2Config extends SlickConfig {
  override val db = Database.forConfig("dbConfig.h2")
  override val driver = slick.jdbc.H2Profile
}
