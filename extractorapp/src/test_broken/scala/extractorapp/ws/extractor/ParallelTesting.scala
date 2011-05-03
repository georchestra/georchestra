package extractorapp.ws.extractor

import org.scalatest.TestFailedException
import org.scalatest.matchers.{
  MustMatchers, MatchResult
}
import scala.actors.{
  Futures,Future
}
import Futures._
import collection.jcl.Conversions._
import java.util.{Map => JMap}
/**
 * A trait to make doing parallel tests easier
 *
 * In order to use mix in this trait and use parallel to start several threads in parallel
 * and ensure that they all complete
 *
 * Example:
 * <pre><code>
 * parallel (
 *   thread ("server"){
 *     // make some assertions
 *   },
 *   thread ("client") {
 *     // contact server
 *   })
 *
 * if a test fails or the test times out (3 seconds is the default timeout)
 * the a TestFailedException will be thrown
 *
 * To change the default time out you can override timeout
 */
trait ParallelTesting {

  def timeout = 30000
  
  private val BLOCKING_METHODS = List(("java.net.PlainSocketImpl","socketAccept"),
                                      ("java.net.SocketInputStream","socketRead0"),
                                      ("sun.nio.ch.FileDispatcher","read0"))

  def parallel (threads:TestFuture*) : Unit = {
    def findThread(name:String)
                  (traces:JMap[Thread,Array[StackTraceElement]]):(Thread,Array[StackTraceElement]) = {
      traces.find {_._1.getName == name}.get
    }

    val futures = threads map {_.run}

    val names = threads map {_.name}
    val result = awaitAll(timeout, futures:_*) zip names.toList

    val firstFailure = result filter {case (result,name) => result.getOrElse(None) != None} headOption
    val timedOutNames = result filter {case (result,name) => result.isEmpty} map {_._2}

    if (!firstFailure.isEmpty) {
      val (Some(Some(error:Throwable)), threadName) = firstFailure.get

      val msg = "In thread '%s' the following assertion failed: %s".format(threadName, error.getMessage)
      throw new TestFailedException(msg, error, 0)
    }

    if (!timedOutNames.isEmpty) {
      val stackTraces = Thread.getAllStackTraces

      def trim(s:Array[StackTraceElement]) = s.takeWhile( !_.getClassName.startsWith("extractor"))

      val failures = timedOutNames map { name => "'"+name +"' at " + trim(findThread(name)(stackTraces)._2).mkString("\n    ") }
      val failureMessage = "The following threads did not complete: \n  " + (failures mkString("\n  ") +"\n\n")
      
      throw new TestFailedException(failureMessage, 0)
    }
}

  def thread(name:String)(fun: => Unit) = new TestFuture(name, fun _)
  def fThread(name:String)(fun: () => Unit) = new TestFuture(name, fun)

  class TestFuture(val name:String, fun: () => Unit) {
    def run : Future[Option[Throwable]] = {
      future {
        Thread.currentThread setName name

        try{
          fun()
          None
        } catch {
          case e => Some(e)
        }
      }
    }
  }
}