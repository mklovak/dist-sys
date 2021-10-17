package com.vbarbanyagra

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.sadliak.grpc._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object App {
  private def startServer(routes: HttpRequest => Future[HttpResponse], port: Int, name: String)
                         (implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    val clientRouteLogged = (request: HttpRequest) => {
      routes(request).andThen {
        case Failure(exception) =>
          system.log.error(s"[${request.method}] ${request.uri}: exception occurred", exception)

        case Success(value) =>
          system.log.info(s"[${request.method}] ${request.uri}: response code = ${value.status}")
      }
    }

    val futureBinding = Http().newServerAt("0.0.0.0", port).bind(clientRouteLogged)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(s"Server ${name} online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error(s"Failed to bind ${name} endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      implicit val sys = context.system

      val userRegistryActor = context.spawn(MessageRegistry(), "UserRegistryActor")
      context.watch(userRegistryActor)

      {
        val routes = new UserRoutes(userRegistryActor)(context.system)
        startServer(routes.userRoutes, port = 8080, name = "HTTP")
      }

      {
        val service = ReplicatedLogHandler(new ReplicatedLogGrpcServiceImpl(userRegistryActor))
        startServer(service, port = 8081, name = "GRPC")
      }

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "SecondaryNodeApp")

    sys.addShutdownHook(system.terminate())
  }
}
