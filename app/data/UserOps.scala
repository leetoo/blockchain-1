package data

import models.{Credentials, User}

import scala.concurrent.Future

trait UserOpsComponent {
  def userOps: UserOps

  trait UserOps {
    def login(credentials: Credentials): Future[Option[User]]
    def create(credentials: Credentials): Future[Option[User]]
  }
}

trait DefaultUserOpsComponent extends UserOpsComponent {
  this: UserDataComponent =>

  class DefaultUserOps extends UserOps {

    override def login(credentials: Credentials): Future[Option[User]] =
      userData.login(credentials)

    override def create(credentials: Credentials): Future[Option[User]] =
      userData.create(credentials)
  }
}
