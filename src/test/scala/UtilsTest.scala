import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import utils.Utils.{booleanCheckWrapper, isStringAValidTimestamp}
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
    isStringAValidTimestamp("2019-03-11T08:23:41.325Z") should be (true)
    isStringAValidTimestamp("2019-01-01T00:00:00.000Z") should be (true)
  }

  "The isStringAValidTimestamp method" should "return false for a invalid timestamp" in {
    isStringAValidTimestamp("2019-03-1108:23:41Z") should be (false)
    isStringAValidTimestamp("2019-01-01/00:00:00Z") should be (false)
    isStringAValidTimestamp("2019-03-11") should be (false)
    isStringAValidTimestamp("08:23:41") should be (false)
    isStringAValidTimestamp("2019-03-11T08:-1:41.111Z") should be (false)
    isStringAValidTimestamp("2019-03-11T08:23:41.325") should be (false)
  }
}
