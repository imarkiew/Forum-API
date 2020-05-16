package failures.adhoc

case class SecretKeyIsInvalidFailure(failure: String = "The secret key for the post is invalid") extends Failure
