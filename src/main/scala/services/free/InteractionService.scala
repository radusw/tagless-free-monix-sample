package services.free

import cats.free.{Free, Inject}
import cats.~>
import monix.eval.Task

object Interactions {
  sealed trait DSL[A]
  final case class Get(input: String) extends DSL[String]
  final case class Print(msg: String) extends DSL[Unit]
}

final class InteractionService[F[_]](implicit I: Inject[Interactions.DSL, F]) {
  import Interactions._
  def get(input: String): Free[F, String] = Free.inject[DSL, F](Get(input))
  def print(msg: String): Free[F, Unit] = Free.inject[DSL, F](Print(msg))
}
object InteractionService {
  implicit def actions[F[_]](implicit I: Inject[Interactions.DSL, F]): InteractionService[F] = new InteractionService[F]
}


final class InteractionInterpreter(read: () => String) extends (Interactions.DSL ~> Task) {
  import Interactions._
  def apply[A](a: DSL[A]) = a match {
    case Get(input) => Task { println(input); read() }
    case Print(msg) => Task { println(msg) }
  }
}
