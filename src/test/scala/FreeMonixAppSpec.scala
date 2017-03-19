import scala.concurrent.Await
import scala.concurrent.duration._
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

class FreeMonixAppSpec extends Specification with ScalaCheck {

  val app = new FreeMonixApp()
  implicit val ctx = monix.execution.Scheduler.Implicits.global

  "FreeMonixApp" should {
    "run" in prop { (a: String, b: String) =>
      var aSent = false
      def in(): String =
        if (aSent) b
        else { aSent = true; a }

      val result = Await.result(app.run(in).runAsync, 1.seconds)
      val expected = s"Log: Recorded: $a\nLog: Recorded: $b\nLog: Printed: ${Set(a,b).toList.sorted}"
      result == expected
    }.set(maxSize = 16)
  }
}
