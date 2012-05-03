/**
 * 
 */
package org.geotools.data.ogr;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @author Mauricio Pazos
 *
 */
final class DataProvider  {

    public static final int FEATURES_AMOUNT = 20;
	

    public static SimpleFeatureCollection createFeatureCollectionFullFieldTypes(Geometry geometry, final String crs) throws Exception {

    	SimpleFeatureTypeBuilder tbuilder = new SimpleFeatureTypeBuilder();
        tbuilder.setName("junk");
        tbuilder.setCRS(CRS.decode("EPSG:4326"));
        
        tbuilder.add("the_geom", geometry.getClass());
        tbuilder.add("b", Byte.class);
        tbuilder.add("c", Short.class);
        tbuilder.add("d", Double.class);
        tbuilder.add("e", Float.class);
        tbuilder.add("f", String.class);
        tbuilder.add("g", Date.class);
        tbuilder.add("h", Boolean.class);
        tbuilder.add("i", Number.class);
        tbuilder.add("j", Long.class);
        tbuilder.add("k", BigDecimal.class);
        tbuilder.add("l", BigInteger.class);
        SimpleFeatureType type = tbuilder.buildFeatureType();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(type);
        SimpleFeatureCollection features = FeatureCollections.newCollection();
        for (int i = 0, ii = FEATURES_AMOUNT ; i < ii; i++) {
            features.add(fb.buildFeature(null, new Object[] {
            		geometry, 
            		new Byte((byte) i),
                    new Short((short) i), 
                    new Double(i), 
                    new Float(i), 
                    new String(i + "-string "),
                    new Date(i), 
                    new Boolean(true), 
                    new Integer(22),
                    new Long(1234567890123456789L),
                    new BigDecimal(new BigInteger("12345678901234567890123456789"), 2),
                    new BigInteger("12345678901234567890123456789") }));
        }
        return features;
    }
    
    public static SimpleFeatureCollection createFeatureCollectionPoint() throws Exception {
    	
    	WKTReader r = new WKTReader();
    	Point geom = (Point) r.read("Point(-158 -55)");
    	
    	return createFeatureCollection(geom, "EPSG:4326");
    }


    public static SimpleFeatureCollection createFeatureCollection(Geometry geometry, final String crs) throws Exception {
        SimpleFeatureTypeBuilder tbuilder = new SimpleFeatureTypeBuilder();
        tbuilder.setName("junk");
        tbuilder.setCRS(CRS.decode(crs));
        
        tbuilder.add("a", geometry.getClass());
        tbuilder.add("b", Integer.class);

        SimpleFeatureType type = tbuilder.buildFeatureType();
        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(type);
        SimpleFeatureCollection features = FeatureCollections.newCollection();
        for (int i = 0, ii = FEATURES_AMOUNT ; i < ii; i++) {
        	
            features.add(fb.buildFeature(null, 
            		new Object[] {
                    				geometry, 
                    				new Integer( i)}));
        }
        
        return features;
    }
	
}
