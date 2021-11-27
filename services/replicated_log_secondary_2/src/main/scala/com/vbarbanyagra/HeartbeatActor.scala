package com.vbarbanyagra

import akka.actor.Actor
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.grpc.GrpcClientSettings
import akka.stream.Materializer

import scala.concurrent.duration._
import com.sadliak.grpc._
import com.vbarbanyagra.MessageRegistry

import java.util.concurrent.TimeUnit
import scala.util.{Failure, Success, Try}

class HeartbeatActor() extends Actor {
  import context._

  object SendHeartbeat

  private val interval: FiniteDuration =
    FiniteDuration(system.settings.config.getDuration("my-app.heartbeat-interval").toNanos, TimeUnit.NANOSECONDS)

  private val clientSettings = GrpcClientSettings.fromConfig("primary-node")

  private val nodeId: String = system.settings.config.getString("my-app.node-id")

  override def preStart(): Unit = {
    self ! SendHeartbeat
  }

  def receive = {
    case SendHeartbeat =>
      sendHeartBeat.andThen { _ =>
        context.system.scheduler.scheduleOnce(interval, self, SendHeartbeat)
      }
  }

  private def sendHeartBeat = {
    ReplicatedLogClient(clientSettings).heartBeat(HeartbeatRequest(nodeId)).andThen {
      case Failure(exception) => system.log.error("Heartbeat failed", exception)
      case Success(_) => system.log.info("Successful heartbeat")
    }
  }
}