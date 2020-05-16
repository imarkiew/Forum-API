package failures.adhoc

case class PostIsNotPresentFailure(override val failure: String = "Post does not exist") extends Failure
