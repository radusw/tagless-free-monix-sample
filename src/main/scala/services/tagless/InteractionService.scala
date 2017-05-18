package services.tagless

trait InteractionService[F[_]] {
  sealed trait DSL[A]
  def get(input: String): F[String]
  def print(msg: String): F[Unit]
}


import monix.eval.Task

final class InteractionInterpreter(read: () => String) extends InteractionService[Task] {
  override def get(input: String) = Task {
    println(input)
    read()
  }
  override def print(msg: String) = Task(println(msg))
}
