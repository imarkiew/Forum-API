package failures.adhoc

case class TopicOrPostIsNotPresentFailure(failure: String = "Topic or post does not exist") extends Failure
