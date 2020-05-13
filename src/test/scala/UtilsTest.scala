import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import utils.Utils.{booleanCheckWrapper, stringToTimestamp}
import validation.ValidationFailure


class UtilsTest extends AnyFlatSpec with Matchers {

  class DummyClass()
  case object DummyFailure extends ValidationFailure("Dummy failure")

  val dummyClass = new DummyClass

  "The booleanCheckWrapper method" should "return properly validated object for true predicate" in {
    val checkerValue = booleanCheckWrapper(true, dummyClass, DummyFailure)
    checkerValue.isValid should be (true)
  }

  "The booleanCheckWrapper method" should "return properly validated object for false predicate" in {
    val checkerValue = booleanCheckWrapper(false, dummyClass, DummyFailure)
    checkerValue.isValid should be (false)
  }

  "The isStringAValidTimestamp method" should "return true for a valid timestamp" in {
    stringToTimestamp("2019-03-11T08:23:41.325Z").isSuccess should be (true)
    stringToTimestamp("2019-01-01T00:00:00.000Z").isSuccess should be (true)
  }

  "The isStringAValidTimestamp method" should "return false for a invalid timestamp" in {
    stringToTimestamp("2019-03-1108:23:41Z").isSuccess should be (false)
    stringToTimestamp("2019-01-01/00:00:00Z").isSuccess should be (false)
    stringToTimestamp("2019-03-11").isSuccess should be (false)
    stringToTimestamp("08:23:41").isSuccess should be (false)
    stringToTimestamp("2019-03-11T08:-1:41.111Z").isSuccess should be (false)
    stringToTimestamp("2019-03-11T08:23:41.325").isSuccess should be (false)
  }
}
