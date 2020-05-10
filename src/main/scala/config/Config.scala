package config

import net.ceedubs.ficus.Ficus._
import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.readers.ArbitraryTypeReader._


case class Config(httpAddress: String, httpPort: Int)


object Config {

  val appConfig = ConfigFactory.load.as[Config]("config")

}
