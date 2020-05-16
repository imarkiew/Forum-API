package dto.requests

import cats.implicits._
import validation.Validatable
import validation.ValidationTypes.ValidationResult


case class NewPostRequestDto(nickname: String, email: String, content: String, topicId: Long, timestamp: String) extends Validatable[NewPostRequestDto] {

  override def validate: ValidationResult[NewPostRequestDto] = (
    validateNickname,
    validateEmail,
    validateContent,
    validateTopicId,
    validateTimestamp
  ).mapN(NewPostRequestDto.apply)

  private def validateNickname: ValidationResult[String] = utils.Utils.validateNickname(nickname)
  private def validateEmail: ValidationResult[String] = utils.Utils.validateEmail(email)
  private def validateContent: ValidationResult[String] = utils.Utils.validateContent(content)
  private def validateTopicId: ValidationResult[Long] = utils.Utils.validateId(topicId)
  private def validateTimestamp: ValidationResult[String] = utils.Utils.validateTimestamp(timestamp)
}
