package services.tagless

trait DataOpService[F[_]] {
  def add(value: String): F[Option[String]]
  def findAll(): F[List[String]]
}


import monix.eval.Task

final class InMemoryDataOpInterpreter extends DataOpService[Task] {
  private[this] val storage = new scala.collection.mutable.HashSet[String]

  override def add(value: String) = Task {
    if (storage.add(value))
      Some(value)
    else
      None
  }
  override def findAll() = Task(storage.toList.sorted)
}
