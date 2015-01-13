/**
 *
 */
package org.georchestra.ldapadmin.ds;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import org.georchestra.lib.sqlcommand.AbstractUpdateCommand;

/**
 * Inserts the <b>token</b> associated to the user in the table "USER_TOKEN".
 *
 * @author Mauricio Pazos
 */
final class InsertUserTokenCommand extends AbstractUpdateCommand{


	private static final String SQL_INSERT= "INSERT INTO "+ DatabaseSchema.SCHEMA_NAME + "." + DatabaseSchema.TABLE_USER_TOKEN+ " ("+DatabaseSchema.UID_COLUMN+","+ DatabaseSchema.TOKEN_COLUMN+ ","+DatabaseSchema.CREATION_DATE_COLUMN+") VALUES (?, ?, ?)";

	private Map<String, Object> rowValues;

	/**
	 * Sets the uid and token in the command.
	 * To
	 *
	 * @param row (UID_COLUMN, value)(TOKEN_COLUMN, value) (TIMESTAMP_COLUMN, value)
	 */
	public void setRowValues(final Map<String, Object> row) {

		assert row.keySet().size() == 3;

		this.rowValues = row;
	}


	@Override
	protected PreparedStatement prepareStatement() throws SQLException {

        assert this.connection != null: "database connection is null, use setConnection";

        PreparedStatement pStmt = this.connection.prepareStatement(SQL_INSERT);

        pStmt.setString(1, (String)this.rowValues.get(DatabaseSchema.UID_COLUMN));
		pStmt.setString(2, (String)this.rowValues.get(DatabaseSchema.TOKEN_COLUMN));
		pStmt.setTimestamp(3, (Timestamp) this.rowValues.get(DatabaseSchema.CREATION_DATE_COLUMN));

		return pStmt;
	}


}
