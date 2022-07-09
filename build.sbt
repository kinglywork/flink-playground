name := "flink-playground"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.14"

resolvers ++= Seq(
  "confluent" at "https://packages.confluent.io/maven/"
)

Compile / mainClass := Some("com.playground.Main")

val catsVersion = "2.7.0"
val flinkVersion = "1.13.6"
val circeVersion = "0.13.0"
val slf4jVersion = "1.7.30"
val logbackVersion = "1.2.3"
val vulcanVersion = "1.7.1"
val confluentVersion = "6.2.0"
val awsKinesisAnalyticsVersion = "1.2.0"

libraryDependencies ++= Seq(
  "org.typelevel"                 %% "cats-core"                      % catsVersion,
  "org.apache.flink"              %% "flink-streaming-scala"          % flinkVersion                % "provided",
  "org.apache.flink"              %% "flink-clients"                  % flinkVersion                % "provided",
  "org.apache.flink"              %% "flink-connector-kafka"          % flinkVersion,
  "com.github.fd4s"               %% "vulcan"                         % vulcanVersion,
  "com.github.fd4s"               %% "vulcan-generic"                 % vulcanVersion,
  "io.circe"                      %% "circe-core"                     % circeVersion,
  "io.circe"                      %% "circe-generic"                  % circeVersion,
  "io.circe"                      %% "circe-parser"                   % circeVersion,
  "io.confluent"                  %  "kafka-avro-serializer"          % confluentVersion,
  "ch.qos.logback"                %  "logback-classic"                % logbackVersion,
  "org.slf4j"                     %  "slf4j-api"                      % slf4jVersion,
  "com.amazonaws"                 %  "aws-kinesisanalytics-runtime"   % awsKinesisAnalyticsVersion,
)

assembly / assemblyOutputPath := new File("target/app.jar")

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", _ @_*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-unused-import",
  "-language:higherKinds",
  "-Ypartial-unification",
  "-Xfatal-warnings"
)

Test / scalacOptions ++= Seq("-Yrangepos")

Test / scalacOptions --= Seq(
  "-Ywarn-value-discard",
  "-Ywarn-numeric-widen"
)
