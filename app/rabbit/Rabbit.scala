package rabbit

import java.util.concurrent.TimeUnit

import akka.actor.Actor
import akka.util.Timeout
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.{Channel, Envelope, ShutdownSignalException, Consumer => RabbitConsumer}
import akka.pattern.ask

import scala.util.{Failure, Success}

object RabbitExtension {
  implicit class RichChannel (val channel: Channel) {
    def consume(queue: String)(handler: (Long, String) => Unit) = {
      channel.basicConsume(queue, new RabbitConsumer {
        override def handleCancel (consumerTag: String): Unit = {}

        override def handleRecoverOk (consumerTag: String): Unit = {}

        override def handleCancelOk (consumerTag: String): Unit = {}

        override def handleDelivery (consumerTag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte]): Unit = {
          val message = new String(body)
          handler(envelope.getDeliveryTag, message)
        }

        override def handleShutdownSignal (consumerTag: String, sig: ShutdownSignalException): Unit = {}

        override def handleConsumeOk (consumerTag: String): Unit = {}
      })
    }
  }
}

trait Ack
object Ack extends Ack
object NoAck extends Ack
trait Consumer {
  this : Actor =>

  val channel: Channel
  val queue: String

  import RabbitExtension._
  import scala.concurrent.ExecutionContext.Implicits.global
  implicit val timeout = Timeout(1000, TimeUnit.SECONDS)

  channel.consume(queue) { (tag, message) =>
    val ack = (self ? message).mapTo[Ack]
    ack.onComplete {
      case Success(Ack) =>
        channel.basicAck(tag, false)
      case Success(NoAck) =>
        channel.basicNack(tag, false, false)
      case Failure(_) =>
        channel.basicNack(tag, false, false)
    }

  }
}

trait Publisher {
  this: Actor =>

  val channel: Channel
  val queue: String

  def transform(message: Any): String

  override def receive: Receive = {
    case msg => channel.basicPublish("", queue, null, transform(msg).getBytes())
  }
}