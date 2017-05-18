import monix.cats._
import monix.eval.Task
import monix.execution.atomic.Atomic
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import services.tagless.{InMemoryDataOpInterpreter, InteractionInterpreter, LogService}

import scala.concurrent.Await
import scala.concurrent.duration._

class TaglessMonixAppSpec extends Specification with ScalaCheck {
  implicit val ctx = monix.execution.Scheduler.Implicits.global

  "TaglessMonixApp" should {

    val app = new TaglessMonixApp()
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


    val app2 = new TaglessMonixApp {
      protected class LogInterpreter2 extends LogService[Task] {
        private[this] val storage = new scala.collection.mutable.ListBuffer[String]

        override def add(a: String) = Task(storage.append(a))
        override def show() = Task(storage.toList.map(_.toUpperCase.drop(5)))
      }

      override def run(in: () => String): Task[String] = {
        implicit val a = new InteractionInterpreter(in)
        implicit val d = new InMemoryDataOpInterpreter()
        implicit val l = new LogInterpreter2()
        program[Task]
      }
    }

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
