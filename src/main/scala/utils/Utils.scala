package utils

import java.sql.Timestamp

import validation.ValidationTypes.ValidationResult
import cats.implicits._
import validation.ValidationFailure
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import scala.util.Try
import java.util.UUID


object Utils {

  val timestampFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

  def booleanCheckWrapper[T, U <: ValidationFailure](predicateResult: Boolean, wrappedVal: T, validationFailure: U): ValidationResult[T] =
    if(predicateResult) wrappedVal.validNel else validationFailure.invalidNel

  def generateSecretKey: String = UUID.randomUUID().toString.replace("-", "")

  def isStringAValidTimestamp(string: String, format: String = timestampFormat): Boolean =
    Try[DateTime](DateTimeFormat.forPattern(format).parseDateTime(string)).isSuccess

  def stringToTimestamp(string: String, format: String = timestampFormat): Timestamp =
    new Timestamp(DateTimeFormat.forPattern(format).parseDateTime(string).getMillis)
}