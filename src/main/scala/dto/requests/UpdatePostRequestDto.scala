package dto.requests

import cats.implicits._
import validation.Validatable
import validation.ValidationTypes.ValidationResult


case class UpdatePostRequestDto(content: String, postId: Long, secretKey: String, timestamp: String) extends Validatable[UpdatePostRequestDto] {

  override def validate: ValidationResult[UpdatePostRequestDto] = (
    validateContent,
    validatePostId,
    validateSecretKey,
    validateTimestamp
  ).mapN(UpdatePostRequestDto.apply)

  private def validateContent: ValidationResult[String] = utils.Utils.validateContent(content)
  private def validatePostId: ValidationResult[Long] = utils.Utils.validateId(postId)
  private def validateSecretKey: ValidationResult[String] = secretKey.validNel
  private def validateTimestamp: ValidationResult[String] = utils.Utils.validateTimestamp(timestamp)
}
