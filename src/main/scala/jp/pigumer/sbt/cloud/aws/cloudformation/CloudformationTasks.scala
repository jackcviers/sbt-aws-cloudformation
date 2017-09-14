package jp.pigumer.sbt.cloud.aws.cloudformation

import jp.pigumer.sbt.cloud.aws.dynamodb.DynamoDBProvider
import jp.pigumer.sbt.cloud.aws.s3.{CreateBucket, S3Provider, UploadTemplates}
import jp.pigumer.sbt.cloud.aws.sts.Sts
import sbt.Def
import sbt.complete.DefaultParsers.spaceDelimited

object CloudformationTasks
  extends S3Provider
  with UploadTemplates

  with CloudFormationProvider
  with CreateStack
  with DeleteStack
  with UpdateStack
  with ValidateTemplate
  with ListExports
  with ListStacks

  with DynamoDBProvider
  with CreateBucket
  with Sts {

  def getValueTask = Def.inputTask {
    import jp.pigumer.sbt.cloud.aws.cloudformation.CloudformationPlugin.autoImport._

    spaceDelimited("<key>").parsed match {
      case Seq(key) ⇒
        awscfListExports.value.filter { export ⇒
          export.name == key
        }.map(_.value).head
      case _ ⇒ sys.error("Usage: <key>")
    }
  }
}