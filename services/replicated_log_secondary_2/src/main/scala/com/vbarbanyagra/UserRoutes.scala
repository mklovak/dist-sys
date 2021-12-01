package com.vbarbanyagra

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.vbarbanyagra.MessageRegistry._

import scala.concurrent.Future

class UserRoutes(messageRegistry: ActorRef[MessageRegistry.Command])(implicit val system: ActorSystem[_]) {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getMessages: Future[Messages] =
    messageRegistry.ask(GetMessages)

  val userRoutes: Route =
    concat(
      pathPrefix("api" / "v1" / "messages") {
        pathEnd {
          get {
            complete(getMessages)
          }
        }
      }
    )
}
