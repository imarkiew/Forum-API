package failures.adhoc

case class TopicIsNotPresentFailure(failure: String = "Topic does not exist") extends Failure
