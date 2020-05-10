package validation

import cats.data.ValidatedNel


object ValidationTypes {

  type ValidationResult[T] = ValidatedNel[ValidationFailure, T]

}
