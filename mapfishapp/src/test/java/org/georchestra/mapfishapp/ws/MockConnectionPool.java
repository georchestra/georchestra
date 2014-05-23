package org.georchestra.mapfishapp.ws;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.georchestra.mapfishapp.model.ConnectionPool;
import org.mockito.Mockito;

public class MockConnectionPool extends ConnectionPool {

	public MockConnectionPool(String jdbcUrl) { 
		super(jdbcUrl);
	}
	
	public Connection getConnection() throws SQLException {
		Connection mockedConnection = Mockito.mock(Connection.class);
		PreparedStatement mockedStatementSet = Mockito.mock(PreparedStatement.class);
		PreparedStatement mockedStatementGet = Mockito.mock(PreparedStatement.class);
		PreparedStatement mockedStatementGet2 = Mockito.mock(PreparedStatement.class);
		PreparedStatement mockedStatementGet3 = Mockito.mock(PreparedStatement.class);		

		ResultSet rsGet = Mockito.mock(ResultSet.class);
		ResultSet rsGet2 = Mockito.mock(ResultSet.class);
		
		Mockito.when(mockedConnection.prepareStatement("INSERT INTO mapfishapp.geodocs (username, standard, raw_file_content, "
				+ "file_hash) VALUES (?,?,?,?);"))
			.thenReturn(mockedStatementSet);
		
		
		Mockito.when(mockedConnection.prepareStatement("SELECT count(*)::integer from mapfishapp.geodocs WHERE file_hash = ?;"))
			.thenReturn(mockedStatementGet);

		Mockito.when(mockedConnection.prepareStatement("SELECT raw_file_content from mapfishapp.geodocs WHERE file_hash = ?;"))
		.thenReturn(mockedStatementGet2);

		Mockito.when(mockedConnection.prepareStatement("UPDATE mapfishapp.geodocs set last_access = now() , access_count = "
				+ "access_count + 1 WHERE file_hash = ?;"))
		.thenReturn(mockedStatementGet3);		

		Mockito.when(rsGet.next()).thenReturn(true);
		Mockito.when(rsGet.getInt(Mockito.anyInt())).thenReturn(1);

		Mockito.when(rsGet2.next()).thenReturn(true);
		Mockito.when(rsGet2.getString(Mockito.anyInt())).thenReturn("<ViewContext xmlns=\"http://www.opengis.net/context\" version=\"1.1.0\" id=\"OpenLayers_Context_133\" xsi:schemaLocation=\"http://www.opengis.net/context http://schemas.opengis.net/context/1.1.0/context.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><General><Window width=\"1233\" height=\"342\"/><BoundingBox minx=\"-201405.7589\" miny=\"2245252.767\" maxx=\"598866.8058\" maxy=\"2467226.179\" SRS=\"EPSG:2154\"/><Title/><Extension><ol:maxExtent xmlns:ol=\"http://openlayers.org/context\" minx=\"47680.03567\" miny=\"2267644.975\" maxx=\"349781.0112\" maxy=\"2444833.970\"/></Extension></General><LayerList><Layer queryable=\"0\" hidden=\"0\"><Server service=\"OGC:WMS\" version=\"1.1.1\"><OnlineResource xlink:type=\"simple\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"http://drebretagne-geobretagne.int.lsn.camptocamp.com/geoserver/wms\"/></Server><Name>topp:communes_geofla</Name><Title>communes_geofla</Title><FormatList><Format current=\"1\">image/jpeg</Format></FormatList><StyleList><Style current=\"1\"><Name/><Title>Default</Title></Style></StyleList><Extension><ol:maxExtent xmlns:ol=\"http://openlayers.org/context\" minx=\"47680.03567\" miny=\"2267644.975\" maxx=\"349781.0112\" maxy=\"2444833.970\"/><ol:numZoomLevels xmlns:ol=\"http://openlayers.org/context\">16</ol:numZoomLevels><ol:units xmlns:ol=\"http://openlayers.org/context\">m</ol:units><ol:isBaseLayer xmlns:ol=\"http://openlayers.org/context\">true</ol:isBaseLayer><ol:displayInLayerSwitcher xmlns:ol=\"http://openlayers.org/context\">true</ol:displayInLayerSwitcher><ol:singleTile xmlns:ol=\"http://openlayers.org/context\">false</ol:singleTile></Extension></Layer></LayerList></ViewContext>");
		
		
		Mockito.when(mockedStatementGet.executeQuery()).thenReturn(rsGet);
		Mockito.when(mockedStatementGet2.executeQuery()).thenReturn(rsGet2);
		

		return mockedConnection;
	}
	

}
