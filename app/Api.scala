package epsilon

import com.typesafe.config.ConfigFactory
import data._

object Api extends DefaultUserOpsComponent
  with DefaultUserDataComponent
  with DefaultContractOpsComponent
  with DefaultContractDataComponent
  with DefaultCassandraSessionComponent {

  private[this] val config = ConfigFactory.load()

  override val userOps = new DefaultUserOps
  override val userData = new DefaultUserData

  override val cassandraSession = new DefaultCassandraSession(config)
  override val contractData = new DefaultContractData
  override val contractOps = new DefaultContractOps
}
