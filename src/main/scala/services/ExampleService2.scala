package services

import cats.free.{Free, Inject}
import cats.~>
import monix.eval.Task

class ExampleService2 {
  sealed trait DataOp[A]
  case class Add(value: String) extends DataOp[Option[String]]
  case class FindAll() extends DataOp[List[String]]

  class DataOps[F[_]](implicit I: Inject[DataOp, F]) {
    def add(value: String): Free[F, Option[String]] = Free.inject[DataOp, F](Add(value))
    def findAll: Free[F, List[String]] = Free.inject[DataOp, F](FindAll())
  }
  object DataOps {
    implicit def dataOps[F[_]](implicit I: Inject[DataOp, F]): DataOps[F] = new DataOps[F]
  }

  def inMemoryDataOpInterpreter: DataOp ~> Task = new (DataOp ~> Task) {
    private[this] val storage = new scala.collection.mutable.HashSet[String]

    def apply[A](d: DataOp[A]) = d match {
      case Add(a) => Task { if (storage.add(a)) Some(a) else None }
      case FindAll() => Task { storage.toList.sorted }
    }
  }
}
