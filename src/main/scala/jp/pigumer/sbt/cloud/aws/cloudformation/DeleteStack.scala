package jp.pigumer.sbt.cloud.aws.cloudformation

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.model.{DeleteStackRequest, DescribeStacksRequest}
import sbt.Def.spaceDelimited
import sbt.Keys.streams
import sbt.{Def, Logger}

import scala.util.{Failure, Success, Try}

trait DeleteStack {

  import jp.pigumer.sbt.cloud.aws.cloudformation.CloudformationPlugin.autoImport._

  private def delete(client: AmazonCloudFormation,
                     settings: AwscfSettings,
                     stack: CloudformationStack,
                     log: Logger) = Try {
    val request = new DeleteStackRequest().
      withStackName(stack.stackName.value)

    log.info(stack.stackName.value)
    client.deleteStack(settings.roleARN.map(r ⇒ request.withRoleARN(r)).getOrElse(request))

    new CloudFormationWaiter(client, client.waiters.stackDeleteComplete).wait(
      new DescribeStacksRequest().withStackName(stack.stackName.value)
    )
  }

  def deleteStackTask = Def.inputTask {
    val log = streams.value.log
    val settings = awscfSettings.value
    val client = awscf.value
    spaceDelimited("<shortName>").parsed match {
      case Seq(shortName) =>
        (for {
          stack ← Try(awscfStacks.value.values.getOrElse(shortName, sys.error(s"$shortName of the stack is not defined")))
          _ ← delete(client, settings, stack(), log)
        } yield ()) match {
          case Success(_) ⇒ ()
          case Failure(t) ⇒ {
            log.trace(t)
            sys.error(t.getMessage)
          }
        }
      case _ ⇒ sys.error("Usage: <shortName>")
    }
  }
}
