package config.slick

import slick.jdbc.JdbcBackend.Database


trait PostgresConfig extends SlickConfig {
  override val db = Database.forConfig("dbConfig.postgres")
  override val driver = slick.jdbc.PostgresProfile
}
