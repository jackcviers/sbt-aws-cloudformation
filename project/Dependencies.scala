import sbt.Keys._
import sbt._

object Dependencies {

  val AwsSdkVersion = "1.11.171"

  val AwsCloudformationDeps = Seq(
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-s3" % AwsSdkVersion,
      "com.amazonaws" % "aws-java-sdk-cloudformation" % AwsSdkVersion,
      "com.amazonaws" % "aws-java-sdk-dynamodb" % AwsSdkVersion,

      "org.specs2" %% "specs2-core" % "3.8.6" % Test,
      "org.specs2" %% "specs2-mock" % "3.8.6" % Test,
      "org.specs2" %% "specs2-junit" % "3.8.6" % Test
    )
  )
}
