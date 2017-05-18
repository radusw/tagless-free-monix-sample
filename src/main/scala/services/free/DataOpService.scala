package services.free

import cats.free.{Free, Inject}
import cats.~>
import monix.eval.Task

object DataOps {
  sealed trait DSL[A]
  final case class Add(value: String) extends DSL[Option[String]]
  final case class FindAll() extends DSL[List[String]]
}

final class DataOpService[F[_]](implicit I: Inject[DataOps.DSL, F]) {
  import DataOps._
  def add(value: String): Free[F, Option[String]] = Free.inject[DSL, F](Add(value))
  def findAll: Free[F, List[String]] = Free.inject[DSL, F](FindAll())
}
object DataOpService {
  implicit def dataOps[F[_]](implicit I: Inject[DataOps.DSL, F]): DataOpService[F] = new DataOpService[F]
}


final class InMemoryDataOpInterpreter extends (DataOps.DSL ~> Task) {
  import DataOps._

  private[this] val storage = new scala.collection.mutable.HashSet[String]

  def apply[A](d: DSL[A]) = d match {
    case Add(a) => Task { if (storage.add(a)) Some(a) else None }
    case FindAll() => Task { storage.toList.sorted }
  }
}
