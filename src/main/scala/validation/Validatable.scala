package validation

import validation.ValidationTypes.ValidationResult


// modified implementation from https://www.gregbeech.com/2018/08/12/akka-http-entity-validation/
// and https://typelevel.org/cats/datatypes/validated.html
trait Validatable[T] {
   def validate: ValidationResult[T]
}
