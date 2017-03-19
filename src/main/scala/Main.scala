import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

object Main extends App {
  val appF = new FreeMonixApp().run(() => StdIn.readLine()).runAsync
  println(Await.result(appF, Duration.Inf))
}
