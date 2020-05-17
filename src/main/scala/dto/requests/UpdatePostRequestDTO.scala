package dto.requests

import cats.implicits._
import validation.Validatable
import validation.ValidationTypes.ValidationResult


case class UpdatePostRequestDTO(content: String, postId: Long, secretKey: String, timestamp: String) extends Validatable[UpdatePostRequestDTO] {

  override def validate: ValidationResult[UpdatePostRequestDTO] = (
    validateContent,
    validatePostId,
    validateSecretKey,
    validateTimestamp
  ).mapN(UpdatePostRequestDTO.apply)

  private def validateContent: ValidationResult[String] = utils.Utils.validateContent(content)
  private def validatePostId: ValidationResult[Long] = utils.Utils.validateId(postId)
  private def validateSecretKey: ValidationResult[String] = secretKey.validNel
  private def validateTimestamp: ValidationResult[String] = utils.Utils.validateTimestamp(timestamp)
}
