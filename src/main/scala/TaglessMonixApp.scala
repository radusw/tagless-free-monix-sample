import cats.Monad
import monix.eval.Task
import services.tagless._

class TaglessMonixApp {

  def program[F[_]: Monad](
    implicit
    A: InteractionService[F],
    D: DataOpService[F],
    L: LogService[F]
  ): F[String] = {

    import A._, D._, L._

    import cats.syntax.functor._
    import cats.syntax.flatMap._

    for {
      response <- get("First ?")
      _ <- D.add(response)
      _ <- L.add(s"Log: Recorded: $response")
      response <- get("Second ?")
      _ <- D.add(response)
      _ <- L.add(s"Log: Recorded: $response")
      responses <- findAll()
      _ <- print(responses.toString)
      _ <- L.add(s"Log: Printed: $responses")
      logs <- show()
    } yield logs.mkString("\n")
  }

  def run(in: () => String): Task[String] = {
    import monix.cats._

    implicit val a = new InteractionInterpreter(in)
    implicit val d = new InMemoryDataOpInterpreter()
    implicit val l = new LogInterpreter()
    program[Task]
  }
}
