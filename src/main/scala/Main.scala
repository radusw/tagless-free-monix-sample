import monix.execution.Scheduler.Implicits.global

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.StdIn

object Main extends App {
  println("Free...")
  val appF = new FreeMonixApp().run(() => StdIn.readLine()).runAsync
  println(Await.result(appF, Duration.Inf))

  println("Final Tagless...")
  val appT = new TaglessMonixApp().run(() => StdIn.readLine()).runAsync
  println(Await.result(appT, Duration.Inf))
}
