package services

import cats.free.{Free, Inject}
import cats.~>
import monix.eval.Task

object Logs {
  sealed trait DSL[A]
  final case class Add(a: String) extends DSL[Unit]
  final case class Show() extends DSL[List[String]]
}

final class LogService[F[_]](implicit I: Inject[Logs.DSL, F]) {
  import Logs._
  def add(a: String): Free[F, Unit] = Free.inject[DSL, F](Add(a))
  def show: Free[F, List[String]] = Free.inject[DSL, F](Show())
}
object LogService {
  implicit def logs[F[_]](implicit I: Inject[Logs.DSL, F]): LogService[F] = new LogService[F]
}


final class LogInterpreter extends (Logs.DSL ~> Task) {
  import Logs._

  private[this] val storage = new scala.collection.mutable.ListBuffer[String]

  def apply[A](l: DSL[A]) = l match {
    case Add(a) => Task { storage.append(a) }
    case Show() => Task { storage.toList }
  }
}
