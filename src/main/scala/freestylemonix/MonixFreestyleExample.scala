package freestylemonix

import cats.implicits._
import monix.eval.Task
import monix.cats._

import scala.concurrent.duration.Duration
import scala.concurrent.Await

object FreestyleMonixApp extends App {
  import freestyle._
  import freestyle.implicits._

  @free trait Validation[F[_]] {
    def minSize(s: String, n: Int): FreeS.Par[F, Boolean]
    def hasNumber(s: String): FreeS.Par[F, Boolean]
  }

  @free trait Interaction[F[_]] {
    def tell(msg: String): FreeS[F, Unit]
    def ask(prompt: String): FreeS[F, String]
  }


  @module trait Application[F[_]] {
    val validation: Validation[F]
    val interaction: Interaction[F]
  }


  def program[F[_]](implicit A: Application[F]) = {
    import A._

    for {
      userInput <- interaction.ask("Give me something with at least 3 chars and a number on it")
      valid <- (validation.minSize(userInput, 3) |@| validation.hasNumber(userInput)).map(_ && _).freeS
      _ <- if (valid)
        interaction.tell("awesomesauce!")
      else
        interaction.tell(s"$userInput is not valid")
    } yield ()
  }


  implicit val validationHandler = new Validation.Handler[Task] {
    override def minSize(s: String, n: Int): Task[Boolean] = Task(s.length >= n)
    override def hasNumber(s: String): Task[Boolean] = Task(s.exists(c => "0123456789".contains(c)))
  }

  implicit val interactionHandler = new Interaction.Handler[Task] {
    override def tell(s: String): Task[Unit] = Task.now(println(s))
    override def ask(s: String): Task[String] = Task.now { println(s); "This could have been user input 1" }
  }


  import monix.execution.Scheduler.Implicits.global
  val task = program[Application.Op].exec[Task]
  Await.result(task.runAsync, Duration.Inf)
}
