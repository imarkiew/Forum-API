package failures.validation

import validation.ValidationFailure

object ValidationFailures {
  case object InvalidNicknameLength extends ValidationFailure("The nickname is too short or too long")
  case object InvalidEmailLength extends ValidationFailure("The email is too short or too long")
  case object InvalidEmailAddress extends ValidationFailure("The email format is invalid")
  case object InvalidSubjectLength extends ValidationFailure("The topic is too short or too long")
  case object InvalidContentLength extends ValidationFailure("The comment content is too short or too long")
  case object InvalidTimestamp extends ValidationFailure("The timestamp is invalid")
  case object InvalidNegativeId extends ValidationFailure("The topicId is negative")
}
