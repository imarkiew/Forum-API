name := "Forum-API"

version := "0.1"

scalaVersion := "2.12.4"

resolvers += Resolver.bintrayRepo("hmrc", "releases")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.6.4",
  "com.typesafe.akka" %% "akka-http" % "10.1.11",
  "com.typesafe.akka" %% "akka-stream" % "2.6.4",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.11",
  "com.typesafe" % "config" % "1.4.0",
  "com.iheart" %% "ficus" % "1.4.7",
  "org.typelevel" %% "cats-core" % "2.1.1",
  "uk.gov.hmrc" %% "emailaddress" % "3.4.0",
  "org.scalatest" %% "scalatest" % "3.1.1" % Test
)
