package rabbit

import java.util.UUID

import akka.actor.{Actor, Props}
import com.rabbitmq.client.Channel
import spray.json.DefaultJsonProtocol._
import rabbit.CustomConverters._
import spray.json._

case class ManagerProducerRequest(id: UUID, url: String)
object ManagerProducerRequest {
  implicit val managerFormat = jsonFormat2(ManagerProducerRequest.apply)
}

class ManagerQueueProducer(val channel: Channel, val queue: String) extends Actor with Publisher {
  override def transform (message: Any): String = message match {
    case request: ManagerProducerRequest => request.toJson.compactPrint
  }
}

object ManagerQueueProducer {
  def props(channel: Channel, queue: String) = Props(classOf[ManagerQueueProducer], channel, queue)
}