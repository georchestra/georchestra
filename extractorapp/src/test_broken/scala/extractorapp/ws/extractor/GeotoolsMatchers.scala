package extractorapp.ws.extractor

import org.opengis.referencing.crs._
import org.opengis.filter.Filter;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.expression._
import org.geotools.referencing.CRS.{
  decode, equalsIgnoreMetadata,lookupEpsgCode
}
import org.scalatest.matchers._
import org.scalatest.TestFailedException
import com.vividsolutions.jts.geom.Geometry


/**
 * Defines scalatest Matchers that can be used when testing Geotools objects
 *
 * Matchers are what allow the tests (Specs) to do comparisions.  For examples
 *
 * filter must be (intersects (1,2,3,4))
 * or
 * filter must be (epsg(4326))
 *
 * For more details see <a href="http://www.scalatest.org/scaladoc/doc-1.0/">Scala Test API</a>
 * and <a href="http://www.scalatest.org/scaladoc/doc-1.0/org/scalatest/matchers/Matcher.html">Matchers</a>
 */
trait GeotoolsMatchers extends MustMatchers{
   /** Convenience method for new IntersectsFilter(...) */
   def intersectsFilter (minx : Double, miny : Double, maxx : Double, maxy : Double) =
         new IntersectsFilter(minx, miny, maxx, maxy)

  /** Convenience method for new EpsgMatcher(code) */
  def epsg(code:Int) = new EpsgMatcher(code)

  /**
   * A matcher that verifies that a CRS has a particular epsg code
   */
  class EpsgMatcher(code:Int) extends BeMatcher[CoordinateReferenceSystem]{
    def apply (crs:CoordinateReferenceSystem) = {
      val actualCode = lookupEpsgCode(crs,false)
      new MatchResult(
        crs != null &&
        (actualCode == code || // test
        equalsIgnoreMetadata (decode("EPSG:"+code),crs)),
        "EPSG:%s does not equals EPSG:%s".format(actualCode,code), // failure message
        "EPSG:%s does equals EPSG:%s".format(actualCode,code)  // success message
      )
    }
  }

  /**
   * A matcher that verifies that a given filter is an intersects filter with the supplied bbox
   *
   * Does not check CRS
   */
  class IntersectsFilter (minx : Double, miny : Double, maxx : Double, maxy : Double)
  extends BeMatcher[Filter] {

    private def processExp(exp : Expression) = exp match {
      case literal:Literal => literal.getValue match {
        case geom : Geometry =>
          val envelope = geom.getEnvelopeInternal
        import envelope._
        if((getMinX, getMinY, getMaxX, getMaxY) == (minx, miny, maxx, maxy) ) None
        else Some(envelope + " was not Intersects [%s,%s,%s,%s]" format (minx,miny,maxx,maxy) )
        case unexpected => Some(unexpected + " was not a geometry as expected")
      }
      case _:PropertyName => None
    }

    /* Implementation of matcher */
    def apply(left: Filter) = {
      val result = {
        val intersects = left.asInstanceOf[Intersects]
        val exp1 = intersects.getExpression1
        val exp2 = intersects.getExpression2
        processExp (exp1) orElse processExp (exp2)
      }

      val expectedString = "Intersects [%s,%s,%s,%s]" format (minx,miny,maxx,maxy)
      MatchResult(
        result.isEmpty, // test (sortof) result is actual test
        result.getOrElse("passed"),  // failure message
        "Filter was " + expectedString // success message
      )
    }
  }
}