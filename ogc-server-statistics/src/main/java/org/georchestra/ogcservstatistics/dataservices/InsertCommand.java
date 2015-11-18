/**
 * 
 */
package org.georchestra.ogcservstatistics.dataservices;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import static java.lang.System.out;

/**
 * Insert an ogc service log
 * 
 * @author Mauricio Pazos
 *
 */
public final class InsertCommand extends AbstractDataCommand {

	
	public final static String DATE_COLUMN = "date";
	public final static String USER__COLUMN = "user_name";
	public final static String SERVICE_COLUMN = "service";
	public final static String LAYER_COLUMN = "layer";
	public final static String REQUEST_COLUMN = "request";
	public final static String ORG_COLUMN = "org";
	public final static String SECROLE_COLUMN = "secrole";

	
	private static final String SQL_INSERT= "INSERT INTO ogcstatistics.ogc_services_log("+USER__COLUMN+","+ DATE_COLUMN+ ","+  SERVICE_COLUMN+ "," +LAYER_COLUMN+ "," +REQUEST_COLUMN+ "," +ORG_COLUMN+ ","+SECROLE_COLUMN+") VALUES (?, ?, ?, ?, ?, ?, string_to_array(?, ','))";
	
	private Map<String, Object> rowValues;
	

	public void setRowValues(final Map<String, Object> ogcServiceLog) {
		
		this.rowValues = ogcServiceLog;
	}

	private PreparedStatement prepareStatement() throws SQLException {

        assert this.connection != null: "database connection is null, use setConnection";

        PreparedStatement pStmt = this.connection.prepareStatement(SQL_INSERT);
        pStmt.setString(1, (String)this.rowValues.get(USER__COLUMN));
        
        java.sql.Date sqlDate = new java.sql.Date(((java.util.Date) this.rowValues.get(DATE_COLUMN)).getTime());
		pStmt.setDate(2, sqlDate);
		pStmt.setString(3, ((String)this.rowValues.get(SERVICE_COLUMN)).trim());
        pStmt.setString(4, ((String)this.rowValues.get(LAYER_COLUMN)).trim());
        pStmt.setString(5, ((String)this.rowValues.get(REQUEST_COLUMN)).trim());
        pStmt.setString(6, ((String)this.rowValues.get(ORG_COLUMN)).trim());
        pStmt.setString(7, ((String)this.rowValues.get(SECROLE_COLUMN)).trim());
        
		return pStmt;
	}

	@Override
	public void execute() throws DataCommandException {
		
        assert this.connection != null: "database connection is null, use setConnection";

        // executes the sql statement and checks that the update operation will be inserted one row in the table
        PreparedStatement pStmt=null;
        try {
        	this.connection.setAutoCommit(false);
            pStmt = prepareStatement();
            int updatedRows = pStmt.executeUpdate();
            this.connection.commit();
            
            if(updatedRows < 1){
                throw new DataCommandException("Failed inserting the OGC Service Log. " + pStmt.toString());
            }

        } catch (SQLException e) {
        	if(this.connection != null){
        		try {
					this.connection.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
		            throw new DataCommandException(e.getMessage());
				}
	            throw new DataCommandException(e.getMessage());
        	}
        } finally{
            try {
                if(pStmt != null) pStmt.close();
            	this.connection.setAutoCommit(true);
                
            } catch (SQLException e1) {
                throw new DataCommandException(e1.getMessage());
            } 
        }
		
	}
}
