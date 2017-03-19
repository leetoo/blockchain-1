package data

import com.datastax.driver.core.{Cluster, Session}
import com.typesafe.config.Config

trait CassandraSessionComponent {
  def cassandraSession: CassandraSession

  trait CassandraSession {
    def session: Session
  }
}

trait DefaultCassandraSessionComponent extends CassandraSessionComponent {

  class DefaultCassandraSession(config: Config) extends CassandraSession {
    private[this] val cluster = Cluster.builder()
      .addContactPoint(config.getString("cassandra.host"))
      .withPort(config.getInt("cassandra.port"))
      .build()

    override val session: Session = cluster.connect(config.getString("cassandra.keyspace"))
  }
}
