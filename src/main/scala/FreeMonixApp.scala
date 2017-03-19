import cats._
import cats.data._
import cats.free._
import monix.cats._
import monix.eval.Task
import services._

class FreeMonixApp {
  private[this] val s1 = new ExampleService1()
  private[this] val s2 = new ExampleService2()
  private[this] val s3 = new ExampleService3()

  private type RecordedActionsApp[A] = Coproduct[s2.DataOp, s1.Action, A]
  private type AuditedRecordedActionsApp[A] = Coproduct[s3.Log, RecordedActionsApp, A]

  private def program(implicit
    A: s1.Actions[AuditedRecordedActionsApp],
    D: s2.DataOps[AuditedRecordedActionsApp],
    L: s3.Logs[AuditedRecordedActionsApp]
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
      s2.inMemoryDataOpInterpreter or s1.actionInterpreter(in)
    val auditedRecordedActionsInterpreter: AuditedRecordedActionsApp ~> Task =
      s3.logInterpreter or recordedActionsInterpreter
    val taskInterpreter = auditedRecordedActionsInterpreter

    val taskProgram = program foldMap taskInterpreter
    taskProgram
  }
}
