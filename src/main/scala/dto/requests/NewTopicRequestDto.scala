package dto.requests

import cats.implicits._
import validation.ValidationTypes.ValidationResult
import validation.Validatable


case class NewTopicRequestDto(nickname: String, email: String, subject: String, content: String, timestamp: String) extends Validatable[NewTopicRequestDto] {

  override def validate: ValidationResult[NewTopicRequestDto] = (
    validateNickname,
    validateEmail,
    validateSubject,
    validateContent,
    validateTimestamp
  ).mapN(NewTopicRequestDto.apply)

  private def validateNickname: ValidationResult[String] = utils.Utils.validateNickname(nickname)
  private def validateEmail: ValidationResult[String] = utils.Utils.validateEmail(email)
  private def validateSubject: ValidationResult[String] = utils.Utils.validateSubject(subject)
  private def validateContent: ValidationResult[String] = utils.Utils.validateContent(content)
  private def validateTimestamp: ValidationResult[String] = utils.Utils.validateTimestamp(timestamp)
}
