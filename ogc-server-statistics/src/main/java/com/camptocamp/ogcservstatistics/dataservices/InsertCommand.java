/**
 * 
 */
package com.camptocamp.ogcservstatistics.dataservices;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

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
	
	private static final String SQL_INSERT= "INSERT INTO ogc_services_log("+USER__COLUMN+","+ DATE_COLUMN+ ","+  SERVICE_COLUMN+ "," +LAYER_COLUMN+ "," +REQUEST_COLUMN+ ") VALUES (?, ?, ?, ?, ?)";
	
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
        
		pStmt.setString(3, (String)this.rowValues.get(SERVICE_COLUMN));
        pStmt.setString(4, (String)this.rowValues.get(LAYER_COLUMN));
        pStmt.setString(5, (String)this.rowValues.get(REQUEST_COLUMN));
		
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
