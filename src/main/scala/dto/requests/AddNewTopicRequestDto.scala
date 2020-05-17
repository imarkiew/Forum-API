package dto.requests

import cats.implicits._
import validation.ValidationTypes.ValidationResult
import validation.Validatable


case class AddNewTopicRequestDto(nickname: String, email: String, subject: String, content: String, timestamp: String) extends Validatable[AddNewTopicRequestDto] {

  override def validate: ValidationResult[AddNewTopicRequestDto] = (
    validateNickname,
    validateEmail,
    validateSubject,
    validateContent,
    validateTimestamp
  ).mapN(AddNewTopicRequestDto.apply)

  private def validateNickname: ValidationResult[String] = utils.Utils.validateNickname(nickname)
  private def validateEmail: ValidationResult[String] = utils.Utils.validateEmail(email)
  private def validateSubject: ValidationResult[String] = utils.Utils.validateSubject(subject)
  private def validateContent: ValidationResult[String] = utils.Utils.validateContent(content)
  private def validateTimestamp: ValidationResult[String] = utils.Utils.validateTimestamp(timestamp)
}
