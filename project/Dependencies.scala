import sbt._

object Dependencies {

  object V {
    val akka = "2.4.17"
    val cassandra = "3.1.4"
    val jwt = "0.12.0"
    val rabbitmq = "3.3.1"
  }

  lazy val cassandra = Seq[ModuleID](
    "com.datastax.cassandra" % "cassandra-driver-core" % V.cassandra
  )

  lazy val rabbit = Seq[ModuleID](
    "com.rabbitmq" % "amqp-client" % V.rabbitmq,
    "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.4"
  )

  lazy val akka = Seq[ModuleID](
    "com.typesafe.akka" %% "akka-actor" % V.akka
  )

  val jwt = Seq[ModuleID](
    "com.pauldijou" %% "jwt-play" % V.jwt
  )
}