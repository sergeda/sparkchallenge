package com.playground.input

import com.playground.AWSProfile
import zio.Chunk

final case class InputArguments(inputS3path: String, outputS3Path: String, awsProfile: Option[AWSProfile])

object InputArguments {
  def apply(input: Chunk[String]): InputArguments =
    if (input.length == 3) {
      InputArguments(input(0), input(1), Some(AWSProfile(input(2))))
    } else InputArguments(input(0), input(1), None)
}
