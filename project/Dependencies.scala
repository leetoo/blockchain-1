import sbt._

object Dependencies {

  object V {
    val akka = "2.4.17"
    val cassandra = "3.1.4"
  }

  lazy val cassandra = Seq[ModuleID](
    "com.datastax.cassandra" % "cassandra-driver-core" % V.cassandra
  )

  lazy val akka = Seq[ModuleID](
    "com.typesafe.akka" %% "akka-actor" % V.akka
  )



}