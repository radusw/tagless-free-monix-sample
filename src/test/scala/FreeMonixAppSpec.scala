import cats.~>
import monix.cats._
import monix.eval.Task
import monix.execution.atomic.Atomic

import scala.concurrent.Await
import scala.concurrent.duration._
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import services.{InMemoryDataOpInterpreter, InteractionInterpreter}

class FreeMonixAppSpec extends Specification with ScalaCheck {
  implicit val ctx = monix.execution.Scheduler.Implicits.global

  val app = new FreeMonixApp()
  "FreeMonixApp" should {
    "run" in prop { (a: String, b: String) =>
      val aSent = Atomic(false)
      def in(): String = {
        if (aSent.compareAndSet(expect = false, update = true)) a
        else b
      }

      val result = Await.result(app.run(in).runAsync, 1.seconds)
      val expected = s"Log: Recorded: $a\nLog: Recorded: $b\nLog: Printed: ${Set(a,b).toList.sorted}"
      result == expected
    }.set(maxSize = 16)
    
    final class LogInterpreter2 extends (services.Logs.DSL ~> Task) {
      import services.Logs._
      private[this] val storage = new scala.collection.mutable.ListBuffer[String]
      def apply[A](l: DSL[A]) = l match {
        case Add(log) => Task { storage.append(log) }
        case Show() => Task { storage.toList.map(_.toUpperCase.drop(5)) }
      }
    }
    final class  FreeMonixApp2 extends FreeMonixApp {
      override def run(in: () => String): Task[String] = {
        val taskInterpreter: AuditedRecordedActionsApp ~> Task =
          new LogInterpreter2()
            .or(new InMemoryDataOpInterpreter().or(new InteractionInterpreter(in)): RecordedActionsApp ~> Task)

        val taskProgram = program foldMap taskInterpreter
        taskProgram
      }
    }
    val app2 = new FreeMonixApp2()
    "be able to use other interpreters" in prop { (a: String, b: String) =>
      val aSent = Atomic(false)
      def in(): String = {
        if (aSent.compareAndSet(expect = false, update = true)) a
        else b
      }
      val result = Await.result(app2.run(in).runAsync, 1.seconds)
      val expected = s"RECORDED: ${a.toUpperCase()}\nRECORDED: ${b.toUpperCase}\nPRINTED: ${Set(a,b).toList.sorted.toString().toUpperCase}"
      result == expected
    }.set(maxSize = 16)

  }
}
