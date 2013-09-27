/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.Query;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.xml.Configuration;
import org.geotools.xml.PullParser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;

/**
 * <p>
 *	Access to the kml file
 * </p>
 * @author Mauricio Pazos
 *
 */
public class KmlFeatureSource {

    private static final Log LOG = LogFactory
                                         .getLog(GeotoolsFeatureReader.class
                                                 .getPackage().getName());

    private Configuration    config;
    private InputStream      inputStream;
    private QName            qname;

    public KmlFeatureSource( InputStream inputStream ) throws IOException{

        this.inputStream = inputStream;

        this.config = getConfig(this.inputStream);
        
        if(config instanceof org.geotools.kml.v22.KMLConfiguration){
            this.qname = org.geotools.kml.v22.KML.Placemark;
        } else {
            this.qname = org.geotools.kml.KML.Placemark;

        }
        
        
    }
    
    private Configuration getConfig(InputStream is) {
        
        // TODO detect the kml version from input stream
        // return new org.geotools.kml.KMLConfiguration();
        return  new org.geotools.kml.v22.KMLConfiguration();
    }


    /**
     * Reads the kml file
     * <p>
     * Note: only Filter.INCLUDE is implemented
     *</p>
     * @param q 
     * @return {@link ListFeatureCollection }
     */
    public ListFeatureCollection getFeatures(Query q) throws IOException {
        
        try{
            
            CoordinateReferenceSystem sourceCRS = q.getCoordinateSystem();
            CoordinateReferenceSystem targetCRS = q.getCoordinateSystemReproject();
            
            MathTransform mathTransform = null;
            if ((targetCRS != null) && !sourceCRS.equals(targetCRS)) {
                mathTransform = CRS.findMathTransform(sourceCRS, targetCRS, true);
            }
    
            PullParser parser = new PullParser(config, this.inputStream, this.qname);
    
            ListFeatureCollection list = null;
            SimpleFeature f;
            
            while ((f = (SimpleFeature)parser.parse()) != null) {
    
                Geometry geom = (Geometry) f.getDefaultGeometry();
                
                int srid = geom.getFactory().getSRID();
                if (srid < 0) {
                    srid = 4326; // set the default
                    sourceCRS = CRS.decode("EPSG:" + srid);
                }
                geom.setSRID(srid);

                if (mathTransform != null) {
                    // transformation is required
                    Geometry reprojectedGeometry = JTS.transform(geom, mathTransform);
                    f.setDefaultGeometry(reprojectedGeometry);
                }
                if (list == null) {
                    list = new ListFeatureCollection(f.getFeatureType());
                }

                list.add(f);
            }
            
            return list;
        } catch (Exception e ){
            LOG.error(e.getMessage());
            throw new IOException(e.getMessage());
        }

    }
    
}
