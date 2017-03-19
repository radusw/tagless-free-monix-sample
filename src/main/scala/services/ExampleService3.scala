package services

import cats.free.{Free, Inject}
import cats.~>
import monix.eval.Task

class ExampleService3 {
  sealed trait Log[A]
  case class Add(a: String) extends Log[Unit]
  case class Show() extends Log[List[String]]

  class Logs[F[_]](implicit I: Inject[Log, F]) {
    def add(a: String): Free[F, Unit] = Free.inject[Log, F](Add(a))
    def show: Free[F, List[String]] = Free.inject[Log, F](Show())
  }
  object Logs {
    implicit def logs[F[_]](implicit I: Inject[Log, F]): Logs[F] = new Logs[F]
  }

  def logInterpreter: Log ~> Task = new (Log ~> Task) {
    private[this] val storage = new scala.collection.mutable.ListBuffer[String]

    def apply[A](l: Log[A]) = l match {
      case Add(a) => Task { storage.append(a) }
      case Show() => Task { storage.toList }
    }
  }
}
