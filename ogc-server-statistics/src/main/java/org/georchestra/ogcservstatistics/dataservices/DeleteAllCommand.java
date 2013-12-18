/**
 * 
 */
package org.georchestra.ogcservstatistics.dataservices;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * WARNING Removes all logs from the table ogc_services_log
 * 
 * @author Mauricio Pazos
 *
 */
final public class DeleteAllCommand extends AbstractDataCommand {

	/**
	 * This method will execute a SQL Delete!
	 * 
	 * @see org.georchestra.ogcservstatistics.dataservices.DataCommand#execute()
	 */
	@Override
	public void execute() throws DataCommandException {

		//PreparedStatement pStmt=null;
		Statement pStmt=null;
        try {
			pStmt = this.connection.createStatement();
			pStmt.execute("DELETE FROM ogcstatistics.OGC_SERVICES_LOG");
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DataCommandException(e);
		} finally{
            try {
                if(pStmt != null) pStmt.close();
                
            } catch (SQLException e1) {
                throw new DataCommandException(e1.getMessage());
            } 
		}
	}
}
