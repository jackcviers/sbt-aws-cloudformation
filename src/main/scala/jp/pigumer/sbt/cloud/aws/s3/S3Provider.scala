package jp.pigumer.sbt.cloud.aws.s3

import cloudformation.AwscfSettings
import com.amazonaws.services.s3.{AmazonS3Client, AmazonS3ClientBuilder}

trait S3Provider {

  def amazonS3Client(settings: AwscfSettings): AmazonS3Client =
    AmazonS3ClientBuilder.
      standard.
      withCredentials(settings.credentialsProvider).
      withRegion(settings.region).
      build.asInstanceOf[AmazonS3Client]
}