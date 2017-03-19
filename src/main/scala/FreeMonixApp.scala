import cats._
import cats.data._
import cats.free._
import monix.cats._
import monix.eval.Task
import services._

class FreeMonixApp {
  protected type RecordedActionsApp[A] = Coproduct[DataOps.DSL, Interactions.DSL, A]
  protected type AuditedRecordedActionsApp[A] = Coproduct[Logs.DSL, RecordedActionsApp, A]

  private def program(implicit
    A: InteractionService[AuditedRecordedActionsApp],
    D: DataOpService[AuditedRecordedActionsApp],
    L: LogService[AuditedRecordedActionsApp]
  ): Free[AuditedRecordedActionsApp, String] = {
    import A._, D._, L._

    for {
      response <- get("First ?")
      _ <- D.add(response)
      _ <- L.add(s"Log: Recorded: $response")
      response <- get("Second ?")
      _ <- D.add(response)
      _ <- L.add(s"Log: Recorded: $response")
      responses <- findAll
      _ <- print(responses.toString)
      _ <- L.add(s"Log: Printed: $responses")
      logs <- show
    } yield logs.mkString("\n")
  }

  protected def interpreter(in: () => String): AuditedRecordedActionsApp ~> Task = {
    val recordedActionsInterpreter: RecordedActionsApp ~> Task =
      new InMemoryDataOpInterpreter or new InteractionInterpreter(in)
    new LogInterpreter or recordedActionsInterpreter
  }

  def run(in: () => String): Task[String] = {
    program.foldMap(interpreter(in))
  }
}
