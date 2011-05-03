package extractorapp.ws.extractor.wcs

import java.net.{
  Socket, URL, ServerSocket
}

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.concurrent.Conductor
import org.scalatest.matchers.MustMatchers

import scala.io.Source
import scala.collection.jcl.Conversions._

import org.geotools.referencing.CRS
import org.opengis.parameter.{
  GeneralParameterValue, ParameterValueGroup
}

import java.io.File
import java.nio.channels.Channels
import java.net._

import GeotoolsMocks._
import IoSupport._

/**
 * Encapsulates the classes required for configuring the MockWcsServer
 */
object MockWcsServer {
  /**
   * A configuration of an expected request.
   * response is the file to send to the requestor
   * test is the tests to perform on the parameters of the request
   */
  abstract class WcsRequest(val response:File, val test:(String => String) => Unit)
  /**
   * The expectation of a DescribeCoverage request
   */
  case class DescribeCoverage(file:File, t:(String => String) => Unit) extends WcsRequest(file, t)
  object DescribeCoverage {
    def apply() = new DescribeCoverage(file(this.getClass, "../describe-coverage-1.0.0.xml"), _=>())
    def apply(tests:(String => String) => Unit) = new DescribeCoverage(file(this.getClass, "../describe-coverage-1.0.0.xml"), tests)
    def apply(file:File) = new DescribeCoverage(file, _=>())
  }
  /**
   * The expectation of a GetCoverage request
   */
  case class GetCoverage(image:File, t:(String => String) => Unit) extends WcsRequest(image, t)
  object GetCoverage {
    def apply() = new GetCoverage(file(this.getClass, "../image4326.jpg"), _=>())
    def apply(tests:(String => String) => Unit) = new GetCoverage(file(this.getClass, "../image4326.jpg"), tests)
    def apply(image:File) = new GetCoverage(image, _=>())
  }
}

import MockWcsServer._
/**
 * A Mock Server used for testing. It can be configured with requests that will be expected to be
 * called by the client
 *
 * @param requests The expected requests. Order is important
 */
class MockWcsServer(requests: WcsRequest*) extends Function0[Unit] {

  def this() = this(DescribeCoverage(), GetCoverage())
  def this(image:File) = this(DescribeCoverage(), GetCoverage(image))

  /**
   * A second way of configuring which requests are expected.
   *
   * EG new MockWcsServer() accepting (DescribeCoverage())
   *
   * Makes it clear when reading the code what the parameters do
   */
  def accepting (req : WcsRequest*) = new MockWcsServer(req:_*)

  /**
   * the port the server is listening to
   */
  def port = openSocket._1

  /**
   * start the server
   */
  def apply() = {
    val server = serverSocket
    try{
      for(req <- requests){
        accept(req, server)
      }
    }finally{
      server.close
    }
  }

  // ----------  Support methods  ----------- //
  private val startPort = 8765

  private lazy val openSocket = {
   def open(port:Int) : (Int,ServerSocket) = {
      try {(port,new ServerSocket(port))}
      catch {case _ => open(port+1)}
    }
    open(startPort)
  }

  private val serverSocket = openSocket._2

  private def accept(req:WcsRequest, server:ServerSocket) = {
    val socket = server.accept()
    try {
      val in = socket.getInputStream
      val paramMap = httpRequest(in) match {
        case Post(_,_,post) =>
          val params = post.lines map { line =>
                                  val parts = line.split("=").take(2)
                                  (parts(0).toUpperCase, parts(1).toUpperCase)
                                }
          Map (params.toList:_*)
        case get:Get => get.query
      }

      req.test(paramMap)

      writeHttpResponse (socket, req.response)

    }finally{
      socket.close
    }
  }

}