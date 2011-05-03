/*
 * This file contains classes and methods for doing IO.
 *
 * One of the things I like to do is set up a ServerSocket that
 * accepts requests from clients so that the requests can be inspected
 * for validity.
 */

package extractorapp.ws.extractor

import java.nio.ByteBuffer
import java.nio.channels.{
  FileChannel, Channels
}
import java.io.{
  FileOutputStream,FileInputStream, 
  InputStream, File
}
import scala.Math._
import java.net.Socket
import File.separator

/**
 * Represents an HTTP request that has been made to a ServerSocket
 */
abstract class HttpRequest(queryString:String) {
  /**
   * convert query string to a map of params
   */
  def query : Map[String,String] = {
    val params = queryString.toUpperCase.trim split "&"
    Map (
      params map { p =>
        val parts = p split ("=")
        val key = parts(0).dropWhile {_ == '?'}.mkString
        if (parts.size == 1) (key,"")
        else (key,parts(1))
      }:_*
    )
  }
}

/**
 * An HTTP POST request
 */
case class Post(headers:Seq[String], queryString:String, post:String)
  extends HttpRequest(queryString)
  /**
   * An HTTP GET request
   */
case class Get(headers:Seq[String], queryString:String)
  extends HttpRequest(queryString)

object IoSupport {
  /**
   * Find a file required for testing.  The lookup class is used as the base directory
   * for the searching
   */
	def file(lookupClass : Class[_], fileName : String) : File = {
    val base = lookupClass.getResource(".").getFile
    new File(base + separator + fileName)
	}

	/**
	 * Open a file channel to a test file
	 */
  def fileChannel(lookupClass : Class[_], fileName : String) : FileChannel = {
    val f = file(lookupClass, fileName)
    new FileInputStream(f).getChannel
  }

  def copy(from:File, to:File) = {
    val in = new FileInputStream(from)
    val out = new FileOutputStream(to)
    try {
      in.getChannel.transferTo(0,from.length,out.getChannel)
    } finally{
      in.close
      out.close
    }
  }

  /**
   * Write out the file to the socket
   */
  def writeHttpResponse (socket:Socket, file:File) = {
    val channel = new FileInputStream(file).getChannel
    val to = Channels.newChannel(socket.getOutputStream)
    try{
      channel transferTo (0,MAX_INT, to)
    } finally {
      to.close
      channel.close
    }
  }

  /**
   * If a HttpRequest is made to a socket this method will process the input
   * stream and create a request object from it
   *
   * Does not have to be really robust because it is for mocking a server
   */
  def httpRequest (in : InputStream) : HttpRequest = {
    val b = new StringBuilder();
    var headers = List[String]()
    var contentLength = 0;
    var post = ""
    var operation = ""
    var query = ""

    var n = in.read

    while(n != -1) {
      b.append(n.toChar)
      val line = b.toString

      if(line.endsWith("\r\n")){
        b setLength 0

        line.toUpperCase match {
          case line if(operation == "") =>  // if operation is "" then it is the first line, so get the request out
            operation = line.takeWhile {_!=' '}.mkString
            query = line.drop (operation.length+1).toString
          case line if(line.trim.startsWith("CONTENT-LENGTH")) =>
            headers = line :: headers
            contentLength = line.split(":")(1).trim.toInt
            n = in.read
          case "\r\n" if(contentLength > 0) =>
            val reading = new Array[Byte] (contentLength)
            in.read(reading)
            post = new String(reading)
            n = -1;
          case "\r\n"  =>
            n = -1
          case l =>
            headers = line :: headers
            n = in.read
        }
      } else {
        n = in.read
      }
    }

    operation.trim.toUpperCase match {
      case "GET" => Get(headers, query)
      case "POST" => Post(headers, query, post)
      case protocol => throw new UnsupportedOperationException("'"+protocol+"' not supported")
    }
  }
}