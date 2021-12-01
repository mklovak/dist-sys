package com.vbarbanyagra

import akka.actor.Actor
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.pattern.ask
import akka.grpc.GrpcClientSettings
import akka.stream.Materializer
import akka.util.Timeout

import scala.concurrent.duration._
import com.sadliak.grpc._
import com.vbarbanyagra.MessageRegistry
import com.vbarbanyagra.MessageRegistry.GetFirstMissingId

import java.util.concurrent.TimeUnit
import scala.util.{Failure, Success, Try}

class HeartbeatActor(messageRegistry: ActorRef[MessageRegistry.Command]) extends Actor {
  import context._
  implicit val scheduler: Scheduler = schedulerFromActorSystem(system.toTyped)

  private implicit val timeout: Timeout = Timeout.durationToTimeout(10.seconds)

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
    val request = (ref: ActorRef[FirstMissingId]) => GetFirstMissingId(replyTo=ref)

    (messageRegistry ? request).flatMap { case FirstMissingId(id) =>
      val hbRequest = HeartbeatRequest(nodeId=nodeId, firstMissingId=id)
      ReplicatedLogClient(clientSettings).heartBeat(hbRequest).andThen {
        case Failure(exception) => system.log.error("Heartbeat failed", exception)
        case Success(_) => system.log.info(s"Successful heartbeat, first missing is ${id}")
      }
    }
  }
}