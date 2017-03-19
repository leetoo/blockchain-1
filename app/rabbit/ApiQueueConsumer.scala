package rabbit

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import spray.json.DefaultJsonProtocol._
import CustomConverters._
import akka.util.Timeout
import com.rabbitmq.client.Channel
import com.typesafe.config.ConfigFactory
import spray.json._
case class ApiResponse(id: UUID, probability: Double)
object ApiResponse {
  implicit val apiFormat = jsonFormat2(ApiResponse.apply)
}

class ApiQueueConsumer(listener: ActorRef, val channel: Channel, val queue: String) extends Actor with Consumer {
  override def receive: Receive = {
    case message: String =>
      println(s"Got message $message")
      val response = message.parseJson.convertTo[ApiResponse]
      println(response)
      listener ! response
      sender ! Ack
  }

}
object ApiQueueConsumer {
  def props(listener: ActorRef, channel: Channel, queue: String) = Props(classOf[ApiQueueConsumer], listener, channel, queue)
}

case class WorkRequest(id: UUID, url: String)
class ApiManager(managerProducer: ActorRef) extends Actor {

  val requestsMap = scala.collection.mutable.Map[UUID, ActorRef]()

  override def receive: Receive = {
    case WorkRequest(id, url) =>
      println(s"Got request for $id")
      val managerRequest = ManagerProducerRequest(id, url)
      requestsMap +=  id -> sender
      managerProducer ! managerRequest

    case response: ApiResponse =>

      println(s"Got response for ${response.id}")

      requestsMap(response.id) ! response
      requestsMap - response.id

  }
}
object ApiManager {
  def props(managerProducer: ActorRef) = Props(classOf[ApiManager], managerProducer)
}

object Test extends App {
  import akka.pattern.ask
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val timeout = Timeout(200, TimeUnit.SECONDS)

  val system = ActorSystem("test")

  val config = ConfigFactory.load()

  val connection = Queues.rabbit(config.getConfig("queues")).newConnection()
  val channel = connection.createChannel()

  val managerProducer = system.actorOf(ManagerQueueProducer.props(channel, config.getString("manager.consumer.address")))

  val apiManager = system.actorOf(ApiManager.props(managerProducer))

  val apiConsumer = system.actorOf(ApiQueueConsumer.props(apiManager, channel, config.getString("api.result.address")))

  val f = apiManager ? WorkRequest(UUID.randomUUID(), "http://dou.ua")

  f.onComplete(println)

}