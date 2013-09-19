/**
 * 
 */
package org.georchestra.ldapadmin.ds;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.georchestra.lib.sqlcommand.AbstractUpdateCommand;

/**
 * Deletes the user_token association.
 * 
 * @author Mauricio Pazos
 *
 */
final class DeleteUserTokenCommand extends AbstractUpdateCommand{


	private static final String SQL= "DELETE FROM "+ DatabaseSchema.TABLE_USER_TOKEN + " WHERE "+ DatabaseSchema.UID_COLUMN + " = ?";
	
	private String uid;
	
	public void setUid(String uid) {
		
		this.uid =uid;
	}


	@Override
	protected PreparedStatement prepareStatement() throws SQLException {

        assert this.connection != null: "database connection is null, use setConnection";

        PreparedStatement pStmt = this.connection.prepareStatement(SQL);

        pStmt.setString(1, this.uid);
		
		return pStmt;
	}
	
}
