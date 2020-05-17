package dto.requests

import cats.implicits._
import validation.Validatable
import validation.ValidationTypes.ValidationResult


case class DeletePostRequestDTO(postId: Long, secretKey: String) extends Validatable[DeletePostRequestDTO] {

  override def validate: ValidationResult[DeletePostRequestDTO] = (validatePostId, validateSecretKey).mapN(DeletePostRequestDTO.apply)

  private def validatePostId: ValidationResult[Long] = utils.Utils.validateId(postId)
  private def validateSecretKey: ValidationResult[String] = secretKey.validNel
}
