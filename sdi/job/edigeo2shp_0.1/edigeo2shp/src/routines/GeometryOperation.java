// ============================================================================
//
// Copyright (C) 2007-2008 Camptocamp - www.camptocamp.com
//				 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the  agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//   
// ============================================================================
package routines;

import org.talend.sdi.geometry.Geometry;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Provide operations on geometry to be process mainly in expression of a tMap component. 
 * 
 * @author mcoudert
 * @author fxprunayre
 */
public class GeometryOperation {

    /**
     * GETLENGTH( ) Returns the length of the specified geometry.
     * 
     * {talendTypes} double | Double
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {example} GETAREA(null)
     * 
     */
    public static Double GETLENGTH(Geometry geom) {
	return geom.getLength();
    }

    /**
     * GETAREA( ) Returns the area of the specified geometry.
     * 
     * {talendTypes} double | Double
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {example} GETAREA(null)
     * 
     */
    public static Double GETAREA(Geometry geom) {
	return geom.getArea();
    }

    /**
     * GETNUMPOINTS( ) Returns the number of points in the specified geometry.
     * 
     * {talendTypes} int | Int
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {example} GETNUMPOINTS(null)
     * 
     */
    public static int GETNUMPOINTS(Geometry geom) {
	return geom.getNumPoints();
    }

    /**
     * GETNUMGEOMETRIES( ) Returns the number of geometries in the specified
     * geometry.
     * 
     * {talendTypes} int | Int
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {example} GETNUMGEOMETRIES(null)
     * 
     */
    public static int GETNUMGEOMETRIES(Geometry geom) {
	return geom.getNumGeometries();
    }

    /**
     * GETGEOMETRYTYPE( ) Returns the type of the specified geometry.
     * 
     * {talendTypes} string | String
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {example} GETGEOMETRYTYPE(null)
     * 
     */
    public static String GETGEOMETRYTYPE(Geometry geom) {
	return geom.getGeometryType();
    }

    /**
     * GETSRID( ) Returns the SRID of the specified geometry.
     * 
     * {talendTypes} int | Int
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {example} GETSRID(null)
     * 
     */
    public static int GETSRID(Geometry geom) {
	return geom.getSRID();
    }

    /**
     * EQUALS( ) Returns true if the two geometries are equal.
     * 
     * {talendTypes} boolean | Boolean
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {param} Geometry(null)
     * 
     * {example} EQUALS(null, null)
     * 
     */
    public static boolean EQUALS(Geometry geom1, Geometry geom2) {
	return geom1.equals(geom2);
    }

    /**
     * DISTANCE( ) Returns the distance between the two geometries.
     * 
     * {talendTypes} double | Double
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {param} Geometry(null)
     * 
     * {example} DISTANCE(null, null)
     * 
     */
    public static double DISTANCE(Geometry geom1, Geometry geom2) {
	return geom1.distance(geom2);
    }

    /**
     * GETCENTROID( ) Returns the centroid of the specified geometry.
     * 
     * {talendTypes} geometry | Geometry
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {example} GETCENTROID(null)
     * 
     */
    public static Geometry GETCENTROID(Geometry geom) {
	return geom.getCentroid();
    }

    /**
     * GETINTERIORPOINT( ) Returns a point located inside the specified
     * geometry.
     * 
     * {talendTypes} geometry | Geometry
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {example} GETINTERIORPOINT(null)
     * 
     */
    public static Geometry GETINTERIORPOINT(Geometry geom) {
	return geom.getInteriorPoint();
    }

    /**
     * GETENVELOPE( ) Returns the envelope of the specified geometry.
     * 
     * {talendTypes} geometry | Geometry
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {example} GETENVELOPE(null)
     * 
     */
    public static Geometry GETENVELOPE(Geometry geom) {
	return geom.getEnvelope();
    }

    /**
     * GETBOUNDARY( ) Returns the boundary of the specified geometry.
     * 
     * {talendTypes} geometry | Geometry
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {example} GETBOUNDARY(null)
     * 
     */
    public static Geometry GETBOUNDARY(Geometry geom) {
	return geom.getBoundary();
    }

    /**
     * GETCONVEXHULL( ) Returns the convex hull polygon of the specified
     * geometry.
     * 
     * {talendTypes} geometry | Geometry
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {example} GETCONVEXHULL(null)
     * 
     */
    public static Geometry GETCONVEXHULL(Geometry geom) {
	return geom.convexHull();
    }

    /**
     * GETGEOMETRYN( ) Returns the n th geometries of the specified geometry.
     * 
     * {talendTypes} geometry | Geometry
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {param} int(null)
     * 
     * {example} GETGEOMETRYN(null, null)
     * 
     */
    public static Geometry GETGEOMETRYN(Geometry geom, int n) {
	return geom.getGeometryN(n);
    }

    /**
     * GETCOORDINATE( ) Returns the x, y or z coordinate of the n th coordinate
     * of the specified geometry. 0 is first coordinate. If n equals -1, then
     * the last pair of coordinate is returned. If n is greater than the number
     * of coordinates, -1 is returned.
     * 
     * {talendTypes} geometry | Geometry
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {param} int(-1) pair of coordinates
     * 
     * {param} String("X") "X", "Y" or "Z" value
     * 
     * {example} GETCOORDINATE(null, -1, "X")
     * 
     */
    public static double GETCOORDINATE(Geometry geom, int n, String xyz) {
	Coordinate[] coords = geom.getCoordinates();

	// return last one
	if (n == -1)
	    if (xyz.equalsIgnoreCase("X"))
		return coords[coords.length - 1].x;
	    else if (xyz.equalsIgnoreCase("Y"))
		return coords[coords.length - 1].y;
	    else
		return coords[coords.length - 1].z;

	// Not so many points
	if (n >= coords.length)
	    return -1;

	if (xyz.equalsIgnoreCase("X"))
	    return coords[n].x;
	else if (xyz.equalsIgnoreCase("Y"))
	    return coords[n].y;
	else
	    return coords[n].z;
    }

    /**
     * SIMPLIFY( ) Returns a simplified geometry.
     * 
     * {talendTypes} geometry | Geometry
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {param} String("DouglasPeuckerSimplifier") Type, values:
     * "DouglasPeuckerSimplifier", "TopologyPreservingSimplifier"
     * 
     * {param} double(1) tolerance
     * 
     * {example} SIMPLIFY(null, "", 1)
     * 
     */
    public static Geometry SIMPLIFY(Geometry geom, String type, double tolerance) {
	return geom.simplify(type, tolerance);
    }

    /**
     * GETBUFFER( ) Returns the buffer of the specified geometry.
     * 
     * {talendTypes} geometry | Geometry
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {param} double(1) distance
     * 
     * {param} int(8) Quadrant segments, default 8
     * 
     * {param} String("BUTT") End cap style, values: "ROUND", "SQUARE", "BUTT",
     * "FLAT"
     * 
     * {example} GETBUFFER(null, 8, null)
     * 
     */
    public static Geometry GETBUFFER(Geometry geom, double d, int quantization,
	    String endCapStyle) {
	int ecs = com.vividsolutions.jts.operation.buffer.BufferOp.CAP_BUTT;

	if (endCapStyle != null) {
	    if (endCapStyle.equals("ROUND"))
		ecs = com.vividsolutions.jts.operation.buffer.BufferOp.CAP_ROUND;
	    else if (endCapStyle.equals("SQUARE"))
		ecs = com.vividsolutions.jts.operation.buffer.BufferOp.CAP_SQUARE;
	    else if (endCapStyle.equals("FLAT"))
		ecs = com.vividsolutions.jts.operation.buffer.BufferOp.CAP_FLAT;
	}

	return geom.buffer(d, quantization, ecs);
    }

    /**
     * INTERSECTION( ) Returns the intersection of the two geometries specified.
     * 
     * {talendTypes} geometry | Geometry
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {param} Geometry(null)
     * 
     * {example} INTERSECTION(null)
     * 
     */
    public static Geometry INTERSECTION(Geometry geom, Geometry geom1) {
	return geom.intersection(geom1);
    }

    /**
     * UNION( ) Returns the union of the two geometries specified.
     * 
     * {talendTypes} geometry | Geometry
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {param} Geometry(null)
     * 
     * {example} UNION(null)
     * 
     */
    public static Geometry UNION(Geometry geom, Geometry geom1) {
	return geom.union(geom1);
    }

    /**
     * SYMDIFFERENCE( ) Returns the sym difference of the two geometries
     * specified.
     * 
     * {talendTypes} geometry | Geometry
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {param} Geometry(null)
     * 
     * {example} SYMDIFFERENCE(null)
     * 
     */
    public static Geometry SYMDIFFERENCE(Geometry geom, Geometry geom1) {
	return geom.symDifference(geom1);
    }

    /**
     * DIFFERENCE( ) Returns the difference of the two geometries specified.
     * 
     * {talendTypes} geometry | Geometry
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {param} Geometry(null)
     * 
     * {example} DIFFERENCE(null)
     * 
     */
    public static Geometry DIFFERENCE(Geometry geom, Geometry geom1) {
	return geom.difference(geom1);
    }

    /**
     * ISVALID( ) Returns if the geometry is valid or not.
     * 
     * {talendTypes} string | String
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {example} ISVALID(null)
     * 
     */
    public static String ISVALID(Geometry geom) {
	return geom.isValid();
    }

    /**
     * TOWKT( ) Returns the WKT representation of the geometry.
     * 
     * {talendTypes} string | String
     * 
     * {Category} GeometryOperation
     * 
     * {param} Geometry(null)
     * 
     * {example} TOWKT(null)
     * 
     */
    public static String TOWKT(Geometry geom) {
	return geom.toString();
    }

}
