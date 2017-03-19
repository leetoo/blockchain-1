//enablePlugins(DockerPlugin)
enablePlugins(PlayScala)

test in assembly := {}

name := "blockchain"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++=
  Dependencies.cassandra ++
  Dependencies.akka ++
  Dependencies.jwt ++
  Seq(filters)


assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

imageNames in docker := Seq (
  ImageName(
    namespace = Some("hackaton"),
    repository = normalizedName.value,
    tag = Some("1")
  )
)
dockerfile in docker := {
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("openjdk:8")
    add(artifact, artifactTargetPath)
//    expose(9000)
    cmdRaw(s"java -jar $artifactTargetPath")
  }
}