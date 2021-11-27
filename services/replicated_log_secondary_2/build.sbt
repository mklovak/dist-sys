lazy val akkaHttpVersion = "10.2.6"
lazy val akkaVersion    = "2.6.9"

enablePlugins(AkkaGrpcPlugin)
enablePlugins(JavaAppPackaging)

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.vbarbanyagra",
      scalaVersion    := "2.13.4"
    )),
    name := "replicated_log_secondary_2",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
      "ch.qos.logback"    % "logback-classic"           % "1.2.3",

      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                % "3.1.4"         % Test,
    )
)

// server & client
akkaGrpcGeneratedSources := Seq(AkkaGrpc.Client, AkkaGrpc.Server)

inConfig(Compile)(Seq(
  // take protos from "replicated_log_secondary_2/../../proto" == "dist-sys/proto" for local development
  PB.protoSources += baseDirectory.value / ".." / ".." / "proto",

  // "sourceDirectory in Compile" is "src/main", so this adds "src/main/proto_custom" for docker
  PB.protoSources += sourceDirectory.value / "proto"
))
