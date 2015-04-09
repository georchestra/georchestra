/**
 *
 */
package org.georchestra.mapfishapp.ws.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.Query;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.xml.Configuration;
import org.geotools.xml.PullParser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vividsolutions.jts.geom.Geometry;

/**
 * <p>
 *	Access to the kml file. This implementation support kml 2.1 and 2.2.
 *
 *
 * </p>
 * @author Mauricio Pazos
 *
 */
class KmlFeatureSource {

    private static final Log LOG = LogFactory.getLog(GeotoolsFeatureReader.class.getPackage().getName());

    private Configuration    configuration;
    private QName            qname;
    private File             file;

    /**
     * Creates a source to read the features present in the kml file
     *
     * @param file
     * @throws IOException
     * @throws UnsupportedGeofileFormatException If the kml format is not supported
     */
    public KmlFeatureSource( File file) throws IOException, UnsupportedGeofileFormatException{

        this.file = file;

        this.configuration = getConfig(this.file);

        if(configuration instanceof org.geotools.kml.v22.KMLConfiguration){
            this.qname = org.geotools.kml.v22.KML.Placemark;
        } else {
            this.qname = org.geotools.kml.KML.Placemark;

        }


    }

    /**
     * Read the kml version from file in order to set the kml configuration.
     *
     * @param is
     * @return
     * @throws IOException
     * @throws UnsupportedGeofileFormatException
     */
    private Configuration getConfig(File f) throws  IOException, UnsupportedGeofileFormatException {

        // detect the kml version from input stream

        SAXParserFactory factory = SAXParserFactory.newInstance();
        InputStream is = null;
        try {
            SAXParser parser = factory.newSAXParser();

            DefaultHandler handle = getSaxHandleKMLVersion();

            is = new FileInputStream(f);

            parser.parse(is, handle);



        } catch (ParserConfigurationException e) {

            LOG.error(e.getMessage());
            throw new IOException(e.getMessage());

        } catch (SAXException e) {
            // it the configuration  property was not set in the parse process the file is not recognized.
            if( ! (e.getCause() instanceof FundKMLVersionException) ) {
                throw new UnsupportedGeofileFormatException("This file was not recognized as a valid kml format");
            }

        } finally {
            if(is != null) is.close();
        }
        return  this.configuration;
    }

    /**
     * SAX handle to scan the kml version.
     * <p>
     * This process will set the kml configuration property taking into account the file version.
     * </p>
     * @return
     */
    private DefaultHandler getSaxHandleKMLVersion(){

        return new DefaultHandler() {

            public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {


                if(qName.equalsIgnoreCase("kml")){
                    String value = attributes.getValue(0);
                    String version = value.substring(value.length() - 3, value.length());
                    if(version.equals("2.2")){
                        configuration = new org.geotools.kml.v22.KMLConfiguration();
                        throw new SAXException(new FundKMLVersionException() );
                    } else if (version.equals("2.1") || (version.equals("2.0"))){
                        configuration = new org.geotools.kml.KMLConfiguration();
                        throw new SAXException(new FundKMLVersionException() );
                    } else {
                        final String message = "KML version detected: " + version + ". There is no available binding for this version";
                        LOG.error(message);
                        throw new SAXException(message);
                    }

                }
            }
        };

    }

    /**
     * Reads the kml file
     * <p>
     * Note: only Filter.INCLUDE is implemented
     *</p>
     * @param q
     * @return {@link ListFeatureCollection }
     */
    public SimpleFeatureCollection getFeatures(Query q) throws IOException {

        InputStream is = new FileInputStream(this.file);
        try{

            CoordinateReferenceSystem sourceCRS = q.getCoordinateSystem();
            CoordinateReferenceSystem targetCRS = q.getCoordinateSystemReproject();

            MathTransform mathTransform = null;
            if ((targetCRS != null) && !sourceCRS.equals(targetCRS)) {
                mathTransform = CRS.findMathTransform(sourceCRS, targetCRS, true);
            }

            PullParser parser = new PullParser(configuration, is, this.qname);

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
        } finally {
            is.close();
        }

    }

}
