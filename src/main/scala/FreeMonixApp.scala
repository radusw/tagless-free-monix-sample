import cats._
import cats.data._
import cats.free._
import monix.cats._
import monix.eval.Task
import services._

class FreeMonixApp {
  private type RecordedActionsApp[A] = Coproduct[DataOps.DSL, Interactions.DSL, A]
  private type AuditedRecordedActionsApp[A] = Coproduct[Logs.DSL, RecordedActionsApp, A]

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

  def run(in: () => String): Task[String] = {
    val recordedActionsInterpreter: RecordedActionsApp ~> Task =
      new InMemoryDataOpInterpreter or new InteractionInterpreter(in)
    val auditedRecordedActionsInterpreter: AuditedRecordedActionsApp ~> Task =
      new LogInterpreter or recordedActionsInterpreter
    val taskInterpreter = auditedRecordedActionsInterpreter

    val taskProgram = program foldMap taskInterpreter
    taskProgram
  }
}
