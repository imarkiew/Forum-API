package failures.adhoc

case class NegativeParametersFailure(validationFailure: String = "At least one parameter was negative, but should not have been")
