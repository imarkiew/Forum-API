package config

import net.ceedubs.ficus.Ficus._
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.readers.ArbitraryTypeReader._


case class Config(
                   httpAddress: String,
                   httpPort: Int,
                   nicknameMinLength: Int,
                   nicknameMaxLength: Int,
                   emailMinLength: Int,
                   emailMaxLength: Int,
                   subjectMinLength: Int,
                   subjectMaxLength: Int,
                   contentMinLength: Int,
                   contentMaxLength: Int,
                   maxNrOfReturnedTopics: Int,
                   maxNrOfReturnedPosts: Int
                 )


object Config {

  val appConfig = ConfigFactory.load.as[Config]("config")

}
