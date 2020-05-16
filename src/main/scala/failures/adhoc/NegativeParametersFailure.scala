package failures.adhoc

case class NegativeParametersFailure(failure: String = "At least one parameter was negative, but should not have been") extends Failure
