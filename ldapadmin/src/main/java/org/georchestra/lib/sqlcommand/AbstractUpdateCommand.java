/**
 * 
 */
package org.georchestra.lib.sqlcommand;

import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * Executes Insert, Update and Delete SQL command.
 * 
 * <p>
 * The subclass must provide the sql command to execute. To do that the {@link AbstractUpdateCommand#prepareStatement()} method 
 * </p>
 * 
 * 
 * @author Mauricio Pazos
 *
 */
public abstract class AbstractUpdateCommand extends AbstractDataCommand{
	
	

	/**
	 * Execute the sql insert to add the new row (uid, token, timestamp)
	 *  
	 * @see org.georchestra.ogcservstatistics.dataservices.DataCommand#execute()
	 */
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
                throw new DataCommandException("Fail executing. " + pStmt.toString());
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

	
	/**
	 * The subclass should provide a method to prepare Insert, Update or Delete 
	 * @return {@link PreparedStatement}
	 * @throws SQLException
	 */
	protected abstract PreparedStatement prepareStatement() throws SQLException;
	

}
