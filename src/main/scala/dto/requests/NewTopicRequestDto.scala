package dto.requests

import cats.implicits._
import dto.entities.{PostDto, TopicDto, UserDto}
import dto.requests.NewTopicRequestDto._
import validation.ValidationTypes.ValidationResult
import validation.{Validatable, ValidationFailure}
import utils.Utils.booleanCheckWrapper
import uk.gov.hmrc.emailaddress._
import utils.Utils.stringToTimestamp
import config.Config.appConfig


case class NewTopicRequestDto(nickname: String, email: String, subject: String, content: String, timestamp: String) extends Validatable[NewTopicRequestDto] {

  override def validate: ValidationResult[NewTopicRequestDto] = (
    validateNickname,
    validateEmail,
    validateSubject,
    validateContent,
    validateTimestamp
  ).mapN(NewTopicRequestDto.apply)

  private def validateNickname: ValidationResult[String] =
    booleanCheckWrapper(nickname.length >= appConfig.nicknameMinLength && nickname.length <= appConfig.nicknameMaxLength, nickname, InvalidNicknameLength)

  private def validateEmail: ValidationResult[String] = {

    val lengthValidationResult = booleanCheckWrapper(email.length >= appConfig.emailMinLength && email.length <= appConfig.emailMaxLength, email, InvalidEmailLength)
    if (lengthValidationResult.isInvalid) {
      lengthValidationResult
    } else if (!EmailAddress.isValid(email)) {
      InvalidEmailAddress.invalidNel
    } else email.validNel
  }

  private def validateSubject: ValidationResult[String] =
    booleanCheckWrapper(subject.length >= appConfig.subjectMinLength && subject.length <= appConfig.subjectMaxLength, subject, InvalidSubjectLength)

  private def validateContent: ValidationResult[String] =
    booleanCheckWrapper(content.length >= appConfig.contentMinLength && content.length <= appConfig.contentMaxLength, content, InvalidContentLength)

  private def validateTimestamp: ValidationResult[String] = booleanCheckWrapper(stringToTimestamp(timestamp).isSuccess, timestamp, InvalidTimestamp)
}


object NewTopicRequestDto {

  def fromDto(userDto: UserDto, topicDto: TopicDto, postDto: PostDto): NewTopicRequestDto =
    NewTopicRequestDto(userDto.nickname, userDto.email, topicDto.subject, postDto.content, postDto.postTimestamp.toString)

  case object InvalidNicknameLength extends ValidationFailure("The nickname is too short or too long")
  case object InvalidEmailLength extends ValidationFailure("The email is too short or too long")
  case object InvalidEmailAddress extends ValidationFailure("The email format is invalid")
  case object InvalidSubjectLength extends ValidationFailure("The topic is too short or too long")
  case object InvalidContentLength extends ValidationFailure("The comment content is too short or too long")
  case object InvalidTimestamp extends ValidationFailure("The timestamp is invalid")
}
