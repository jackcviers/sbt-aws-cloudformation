package jp.pigumer.sbt.cloud.aws.s3

import cloudformation.AwscfSettings
import cloudformation.CloudformationPlugin.autoImport.awscfSettings
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient
import com.amazonaws.services.cloudformation.model.{CreateStackRequest, Parameter, Stack}
import sbt.Keys.streams
import sbt.complete.DefaultParsers.spaceDelimited
import sbt.{Def, Logger}

import scala.util.{Failure, Success, Try}

trait CreateBucket {

  val amazonCloudFormation: AwscfSettings ⇒ AmazonCloudFormationClient

  protected def waitForCompletion(client: AmazonCloudFormationClient,
                                  stackName: String,
                                  log: Logger): Try[Seq[Stack]]

  private def createStack(settings: AwscfSettings, stackName: String, log: Logger): Try[Unit] = Try {
    import scala.collection.JavaConverters._

    val yaml =
      """
        |AWSTemplateFormatVersion: '2010-09-09'
        |Parameters:
        |  BucketName:
        |    Type: String
        |    Description: BucketName
        |Resources:
        |  Bucket:
        |    Type: AWS::S3::Bucket
        |    Properties:
        |      BucketName: !Ref 'BucketName'
      """.stripMargin
    log.debug(yaml)

    val request = new CreateStackRequest().
      withTemplateBody(yaml).
      withStackName(stackName).
      withParameters(Seq(
        new Parameter().
          withParameterKey("BucketName").
          withParameterValue(settings.bucketName)).asJava)

    val client = amazonCloudFormation(settings)

    log.info(s"Create ${stackName}")
    client.createStack(settings.roleARN.map(request.withRoleARN(_)).getOrElse(request))
    waitForCompletion(client, stackName, log) match {
      case Failure(t) ⇒ throw t
      case Success(r) ⇒ r.foreach(stack ⇒ log.info(s"${stack.getStackName} ${stack.getStackStatus}"))
    }
  }

  def createBucketTask = Def.inputTask {
    val log = streams.value.log
    val settings = awscfSettings.value
    spaceDelimited("<stackName>").parsed match {
      case Seq(stackName) ⇒ {
        createStack(settings, stackName, log) match {
          case Success(_) ⇒ ()
          case Failure(t) ⇒ {
            log.trace(t)
            sys.error(t.getMessage)
          }
        }
      }
      case _ ⇒ sys.error("Usage: createBucket <stackName>")
    }
  }
}