package services.tagless

trait LogService[F[_]] {
  def add(a: String): F[Unit]
  def show(): F[List[String]]
}


import monix.eval.Task

final class LogInterpreter extends LogService[Task] {
  private[this] val storage = new scala.collection.mutable.ListBuffer[String]

  override def add(a: String) = Task(storage.append(a))
  override def show() = Task(storage.toList)
}
