package rabbit

import com.rabbitmq.client.ConnectionFactory
import com.typesafe.config.Config

object Queues {

  def rabbit(config: Config): ConnectionFactory = {
    val f = new ConnectionFactory()

    f.setHost(config.getString("hosts"))
    f.setUsername("guest")
    f.setPassword("guest")

    f
  }
}
