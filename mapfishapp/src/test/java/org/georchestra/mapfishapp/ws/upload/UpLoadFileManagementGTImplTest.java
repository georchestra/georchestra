/**
 * 
 */
package org.georchestra.mapfishapp.ws.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Point;

/**
 * Unit Test for {@link UpLoadFileManagement}
 * 
 * Test the geotools implementation {@link GeotoolsFeatureReader}.
 * 
 * @author Mauricio Pazos
 * 
 */
public class UpLoadFileManagementGTImplTest {

    public UpLoadFileManagementGTImplTest() {
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    @Before
    public void setUp() {
        // If travis-ci is detected, deactivate.
        //
        // we know that the JVM will crash, and our own CI with Jenkins will
        // handle the test correctly anyway.
        //
        // see:
        // https://github.com/travis-ci/travis-ci/issues/5599#issuecomment-182085831
        String onTravisCi = System.getenv("TRAVIS");
        if (onTravisCi != null) {
            assumeFalse("Travis-ci detected, skipping test",
                    onTravisCi.equalsIgnoreCase("true"));
        }
    }
    /**
     * Test method for
     * {@link mapfishapp.ws.upload.UpLoadFileManagement#getFeatureCollectionAsJSON()}
     * .
     * 
     * @throws IOException
     */
    @Test
    public void testSHPAsJSON() throws Exception {

        String fileName = "points-4326.shp";
        String fullName = makeFullName(fileName);

        testGetGeofileToJSON(fullName, null);
    }

    @Test
    public void testSHPAsJSONReporjectedTo2154() throws Exception {

        String fileName = "points-4326.shp";
        String fullName = makeFullName(fileName);

        testGetGeofileToJSON(fullName, "EPSG:2154");
    }

    /**
     * Tests the coordinates order.
     * 
     * @throws Exception
     */
    @Test
    public void testSHPCoordinatesEPSG4326() throws Exception {

        String fileName = "shp_4326_accidents.shp";
        String fullName = makeFullName(fileName);

        String json = testGetGeofileToJSON(fullName, "EPSG:4326");

        assertCoordinateContains(-2.265330624649336, 48.421434814828025, json);
    }

    /**
     * Tests the coordinates order.
     * <p>
     * The retrieved features aren't reprojected. They will be in the base srs.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testSHPCoordinatesNoReprojected() throws Exception {

        String fileName = "shp_4326_accidents.shp";
        String fullName = makeFullName(fileName);

        String json = testGetGeofileToJSON(fullName, null);

        assertCoordinateContains(-2.265330624649336, 48.421434814828025, json);
    }

    @Test
    public void testKML22AsJSON() throws Exception {

        String fileName = "regions.kml";
        String fullName = makeFullName(fileName);

        String regions = testGetGeofileToJSON(fullName, null);

        JSONObject list = new JSONObject(regions);
        JSONArray jsonArray = list.getJSONArray("features");
        JSONObject reg = jsonArray.getJSONObject(0);
        String id = reg.getString("id");

    }

	@Test
	public void testKML22ExtendedData() throws Exception {

		String fileName = "kml_4326_accidents.kml";
		String fullName = makeFullName(fileName);

		String regions = testGetGeofileToJSON(fullName, null);

		JSONObject list = new JSONObject(regions);
		JSONArray jsonArray = list.getJSONArray("features");
		JSONObject reg = jsonArray.getJSONObject(0);

		JSONObject properties = reg.getJSONObject("properties");
		assertNotNull(getJsonFieldValue(properties, "id"));
		assertNotNull(getJsonFieldValue(properties, "date"));
		assertNotNull(getJsonFieldValue(properties, "plage_hora"));
		assertNotNull(getJsonFieldValue(properties, "jour_nuit"));
		assertNotNull(getJsonFieldValue(properties, "meteo"));
		assertNotNull(getJsonFieldValue(properties, "voie_type"));
		assertNotNull(getJsonFieldValue(properties, "milieu"));
		assertNotNull(getJsonFieldValue(properties, "voie_type"));
		assertNotNull(getJsonFieldValue(properties, "tues_nb"));
		assertNotNull(getJsonFieldValue(properties, "milieu"));
		assertNotNull(getJsonFieldValue(properties, "tues_18_24"));
		assertNotNull(getJsonFieldValue(properties, "tues_moto_"));
		assertNotNull(getJsonFieldValue(properties, "tues_pieto"));
		assertNotNull(getJsonFieldValue(properties, "tues_velo_"));
		assertNotNull(getJsonFieldValue(properties, "vehicules_"));
		assertNotNull(getJsonFieldValue(properties, "pl_impliqu"));
		assertNotNull(getJsonFieldValue(properties, "commune"));
		assertNotNull(getJsonFieldValue(properties, "departemen"));
		assertNotNull(getJsonFieldValue(properties, "commentair"));
		assertNotNull(getJsonFieldValue(properties, "consolide"));
		assertNotNull(getJsonFieldValue(properties, "anciennete"));
		assertNotNull(getJsonFieldValue(properties, "f_mois"));
		assertNotNull(getJsonFieldValue(properties, "f_annee"));
	}

    private String getJsonFieldValue(JSONObject properties, String field) {

        String value;
        try {
            value = properties.getString(field);
        } catch (JSONException e) {
            value = null;
        }

        return value;
    }

    /**
     * Tests the coordinates order.
     * 
     * @throws Exception
     */
    @Test
    public void testKMLCoordinatesEPSG4326() throws Exception {

        String fileName = "kml_4326_accidents.kml";
        String fullName = makeFullName(fileName);

        String json = testGetGeofileToJSON(fullName, "EPSG:4326");

        assertCoordinateContains(-2.265330624649336, 48.421434814828025, json);
    }

    /**
     * Read features no reprojected
     * 
     * @throws Exception
     */
    @Test
    public void testGMLAsJSON() throws Exception {

        String fileName = "border.gml";
        String fullName = makeFullName(fileName);

        testGetGeofileToJSON(fullName, null);

    }

    @Test
    public void testMIFAsJSON() throws Exception {

        String fileName = "pigma_regions_POLYGON.mif";
        String fullName = makeFullName(fileName);

        testGetGeofileToJSON(fullName, null);
    }

    /**
     * Tests the coordinates order.
     * 
     * @throws Exception
     */
    @Test
    public void testMIFCoordinatesEPSG4326() throws Exception {

        String fileName = "mif_4326_accidents.mif";
        String fullName = makeFullName(fileName);

        String json = testGetGeofileToJSON(fullName, "EPSG:4326");

        assertCoordinateContains(-2.265330624649336, 48.421434814828025, json);
    }

    @Test
    public void testMIFAsJSONReprojectedTo2154() throws Exception {

        String fileName = "pigma_regions_POLYGON.mif";
        String fullName = makeFullName(fileName);

        testGetGeofileToJSON(fullName, "EPSG:2154");
    }

    /**
     * Tests the coordinates order.
     * 
     * @throws Exception
     */
    @Test
    public void testGMLCoordinatesEPSG4326() throws Exception {

        String fileName = "gml_4326_accidents.gml";
        String fullName = makeFullName(fileName);

        	
        String json = testGetGeofileToJSON(fullName, "EPSG:4326");

        assertCoordinateContains(-2.265330624649336, 48.421434814828025, json);
    }

    /**
     * Tests the coordinates order. The input layer is projected in epsg:4326,
     * the result is reprojected to epsg:3857.
     * 
     * @throws Exception
     */
    @Test
    public void testGMLCoordinatesFrom4326to3857() throws Exception {

        String fileName = "gml_4326_accidents.gml";
        String fullName = makeFullName(fileName);

        String json = testGetGeofileToJSON(fullName, "EPSG:3857");

        assertCoordinateContains(-252175.451614371791948,
                6177255.152005254290998, json);
    }

    /**
     * Assert that the feature in json syntax contains its coordinate in the
     * order x, y.
     * 
     * @param x
     * @param y
     * @param json
     * @throws Exception
     */
    protected void assertCoordinateContains(final double x, final double y,
            final String json) throws Exception {

        FeatureJSON featureJSON = new FeatureJSON();

        FeatureIterator<SimpleFeature> iter = featureJSON
                .streamFeatureCollection(json);

        assertTrue(iter.hasNext()); // the test data only contains one feature

        SimpleFeature f = iter.next();

        Point geom = (Point) f.getDefaultGeometry();

        assertEquals(x, geom.getCoordinate().x, 10e-8);

        assertEquals(y, geom.getCoordinate().y, 10e-8);
    }

    protected String testGetGeofileToJSON(final String fileName, final String epsg) throws Exception {

        assertTrue(fileName.length() > 0);

        String jsonFeatures = getFeatureCollectionAsJSON(fileName, epsg);
        assertNotNull(jsonFeatures);

        return jsonFeatures;
    }

    /**
     * Sets the bridge {@link AbstractFeatureGeoFileReader} with the
     * {@link GeotoolsFeatureReader} implementation, then retrieves the file as
     * Json collection. The return value will be null if the file is empty.
     * 
     * @param fileName
     * @param epsg
     * @return
     * @throws Exception
     */
    protected String getFeatureCollectionAsJSON(final String fileName,
            final String epsg) throws Exception {

        FileDescriptor fd = new FileDescriptor(fileName);
        fd.listOfFiles.add(fileName);
        fd.listOfExtensions.add(FilenameUtils.getExtension(fileName));

        UpLoadFileManagement fm = create();

        fm.setWorkDirectory(FilenameUtils.getFullPath(fileName));
        fm.setFileDescriptor(fd);

        StringWriter out = new StringWriter();
        if (epsg != null) {
            fm.writeFeatureCollectionAsJSON(out, CRS.decode(epsg));
        } else {
            fm.writeFeatureCollectionAsJSON(out, null);
        }
        return out.toString();
	}

	/**
	 * @return UpLoadFileManagement set with geotools implementation
	 */
	protected UpLoadFileManagement create() throws IOException {
		return UpLoadFileManagement.create();
	}

    /**
     * Returns path+fileName
     * 
     * @param fileName
     * @return
     * @throws Exception
     */
    protected String makeFullName(String fileName) throws Exception {

        URL url = this.getClass().getResource(fileName);

        String fullFileName = url.toURI().getPath();

        return fullFileName;
    }


}
