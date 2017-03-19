package data

import java.util.UUID

import com.datastax.driver.core.{ResultSet, ResultSetFuture, Statement}
import com.datastax.driver.core.querybuilder.QueryBuilder
import models.{Credentials, User}

import collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future

trait UserDataComponent {
  def userData: UserData

  trait UserData {
    def login(credentials: Credentials): Future[Option[User]]
    def create(credentials: Credentials): Future[Option[User]]
  }
}

trait CassandraUserDataComponent extends UserDataComponent {
  this: CassandraSessionComponent =>

  class CassandraUserData extends UserData {
    import data.utils.CommonConverters.Futures._
    import play.api.libs.concurrent.Execution.Implicits._


    override def login(credentials: Credentials): Future[Option[User]] =
      cassandraSession.session
        .executeAsync(Queries.loginUser(credentials))
        .map(_.iterator().asScala.toStream.headOption.map(
          row => User(row.getUUID("id"), credentials.login, credentials.password)
        ))

    override def create(credentials: Credentials): Future[Option[User]] =
      login(credentials) flatMap { userOption =>
        if (userOption.isDefined) Future.successful(None)
        else {
          val uuid = UUID.randomUUID()
          val user = User(uuid, credentials.login, credentials.password)
          Future.sequence(Seq(
            ResultSetFutureToScalaFuture(cassandraSession.session
              .executeAsync(Queries.create(uuid, credentials))),
            ResultSetFutureToScalaFuture(cassandraSession.session
              .executeAsync(Queries.insertIntoUsersLogins(user)))
          )).map(_ => Some(user))
        }
      }
  }

  object Queries {
    def loginUser(credentials: Credentials): Statement =
      QueryBuilder.select().column("id")
        .from("users_by_login_and_password")
        .where(QueryBuilder.eq("login", credentials.login))
        .and(QueryBuilder.eq("password", credentials.password))

    def create(uuid: UUID, credentials: Credentials): Statement =
      QueryBuilder.insertInto("users").values(
        Array("id", "login", "password"),
        Array[AnyRef](uuid, credentials.login, credentials.password)
      )

    def insertIntoUsersLogins(user: User): Statement =
      QueryBuilder.insertInto("users_by_login_and_password").values(
        Array("id", "login", "password"),
        Array[AnyRef](user.uuid, user.login, user.password)
      )
  }
}

trait DefaultUserDataComponent extends UserDataComponent {
  import play.api.libs.concurrent.Execution.Implicits._

  class DefaultUserData extends UserData {
    private[this] val users = collection.mutable.ArrayBuffer.empty[User]

    override def login(credentials: Credentials): Future[Option[User]] = Future {
      users.find(user => user.password == credentials.password && user.login == credentials.login)
    }

    override def create(credentials: Credentials): Future[Option[User]] =
      login(credentials) map { userOption =>
        if (userOption.isDefined) None
        else {
          val user = User(UUID.randomUUID(), credentials.login, credentials.password)
          this.users += user
          Some(user)
        }
      }

  }
}
