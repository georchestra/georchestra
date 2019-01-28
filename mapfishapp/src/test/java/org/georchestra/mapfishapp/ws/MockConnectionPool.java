package org.georchestra.mapfishapp.ws;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.mockito.Mockito;

public class MockConnectionPool{

	Connection mockedConnection = Mockito.mock(Connection.class);
	PreparedStatement mockedStatementSet = Mockito.mock(PreparedStatement.class);
	PreparedStatement mockedStatementGet = Mockito.mock(PreparedStatement.class);
	PreparedStatement mockedStatementGet2 = Mockito.mock(PreparedStatement.class);
	PreparedStatement mockedStatementGet3 = Mockito.mock(PreparedStatement.class);		

	ResultSet rsGet = Mockito.mock(ResultSet.class);
	ResultSet rsGet2 = Mockito.mock(ResultSet.class);

    public DataSource create() {
        try {
            setUpMocks();
            DataSource mockDs = Mockito.mock(DataSource.class);
            Mockito.when(mockDs.getConnection()).thenReturn(mockedConnection);
            return mockDs;
        } catch (SQLException e) {
            throw new RuntimeException("shouldn't happen");
        }
    }    

	private void setUpMocks() throws SQLException  {

		
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
		
		
		Mockito.when(mockedStatementGet.executeQuery()).thenReturn(rsGet);
		Mockito.when(mockedStatementGet2.executeQuery()).thenReturn(rsGet2);
	}
	public void setExpectedDocument(String s) throws SQLException {
		Mockito.when(rsGet2.getString(Mockito.anyInt())).thenReturn(s);
	}

}
