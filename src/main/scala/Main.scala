import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import config.Config.appConfig
import json.converter.JsonConverter
import model.db.impl.PostgresDBImpl


object Main extends App
  with HttpService
  with JsonConverter {

  override implicit val system = ActorSystem("forum-api")
  override implicit def executor = system.dispatcher
  override val dbApi = PostgresDBImpl

  Http().bindAndHandle(routes, appConfig.httpAddress, appConfig.httpPort)
}