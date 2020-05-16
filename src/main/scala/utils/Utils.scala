package utils

import java.time.Instant
import validation.ValidationTypes.ValidationResult
import cats.implicits._
import validation.ValidationFailure
import scala.util.Try
import java.util.UUID
import config.Config.appConfig
import failures.validation.ValidationFailures._
import uk.gov.hmrc.emailaddress.EmailAddress



object Utils {

  def booleanCheckWrapper[T, U <: ValidationFailure](predicateResult: Boolean, wrappedVal: T, validationFailure: U): ValidationResult[T] =
    if(predicateResult) wrappedVal.validNel else validationFailure.invalidNel

  def generateSecretKey: String = UUID.randomUUID().toString.replace("-", "")

  def stringToTimestamp(string: String): Try[Instant] = Try[Instant](Instant.parse(string))

  def validateNickname(nickname: String): ValidationResult[String] =
    booleanCheckWrapper(nickname.length >= appConfig.nicknameMinLength && nickname.length <= appConfig.nicknameMaxLength, nickname, InvalidNicknameLength)

  def validateEmail(email: String): ValidationResult[String] = {
    val lengthValidationResult = booleanCheckWrapper(email.length >= appConfig.emailMinLength && email.length <= appConfig.emailMaxLength, email, InvalidEmailLength)
    if (lengthValidationResult.isInvalid) {
      lengthValidationResult
    } else if (!EmailAddress.isValid(email)) {
      InvalidEmailAddress.invalidNel
    } else email.validNel
  }

  def validateSubject(subject: String): ValidationResult[String] =
    booleanCheckWrapper(subject.length >= appConfig.subjectMinLength && subject.length <= appConfig.subjectMaxLength, subject, InvalidSubjectLength)

  def validateContent(content: String): ValidationResult[String] =
    booleanCheckWrapper(content.length >= appConfig.contentMinLength && content.length <= appConfig.contentMaxLength, content, InvalidContentLength)

  def validateTimestamp(timestamp: String): ValidationResult[String] = booleanCheckWrapper(stringToTimestamp(timestamp).isSuccess, timestamp, InvalidTimestamp)

  def validateId(id: Long): ValidationResult[Long] = booleanCheckWrapper(id >= 0, id, InvalidNegativeId)
}