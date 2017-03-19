package services

import cats.free.{Free, Inject}
import cats.~>
import monix.eval.Task

class ExampleService1 {
  sealed trait Action[A]
  case class Get(input: String) extends Action[String]
  case class Print(msg: String) extends Action[Unit]

  class Actions[F[_]](implicit I: Inject[Action, F]) {
    def get(input: String): Free[F, String] = Free.inject[Action, F](Get(input))
    def print(msg: String): Free[F, Unit] = Free.inject[Action, F](Print(msg))
  }
  object Actions {
    implicit def actions[F[_]](implicit I: Inject[Action, F]): Actions[F] = new Actions[F]
  }

  def actionInterpreter(read: () => String): Action ~> Task = new (Action ~> Task) {
    def apply[A](a: Action[A]) = a match {
      case Get(input) => Task { println(input); read() }
      case Print(msg) => Task { println(msg) }
    }
  }
}
