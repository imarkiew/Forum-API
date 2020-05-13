package utils

import java.time.Instant
import validation.ValidationTypes.ValidationResult
import cats.implicits._
import validation.ValidationFailure
import scala.util.Try
import java.util.UUID


object Utils {

  def booleanCheckWrapper[T, U <: ValidationFailure](predicateResult: Boolean, wrappedVal: T, validationFailure: U): ValidationResult[T] =
    if(predicateResult) wrappedVal.validNel else validationFailure.invalidNel

  def generateSecretKey: String = UUID.randomUUID().toString.replace("-", "")

  def stringToTimestamp(string: String): Try[Instant] = Try[Instant](Instant.parse(string))
}