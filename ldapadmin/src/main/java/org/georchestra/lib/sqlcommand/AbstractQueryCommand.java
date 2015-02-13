/**
 *
 */
package org.georchestra.lib.sqlcommand;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Maintains the abstract behavior required to execute a SQL query.
 * The subclass must implement the methods:
 * <pre>
 * {@link AbstractQueryCommand#prepareStatement()}
 * {@link AbstractQueryCommand#getRow(ResultSet)}
 * </pre>
 *
 * @author Mauricio Pazos
 */
public abstract class AbstractQueryCommand extends AbstractDataCommand {

	private LinkedList<Map<String,Object>> resultList;

	/**
	 * This template method executes the sql statement specified in the prepareStatement method.
	 */
	@Override
	public void execute() throws DataCommandException {

        assert (this.connection != null) : "database connection is null, use setConnection";

        // executes the sql statement and  populates the list with the data present in the result set
        ResultSet rs = null;
        PreparedStatement pStmt=null;
        try {
            pStmt = prepareStatement();

            rs = pStmt.executeQuery();

			this.resultList = new LinkedList<Map<String,Object>>();

            while (rs.next()) {
                this.resultList.add( getRow(rs));
            }

        } catch (SQLException e) {

            throw new DataCommandException(e.getMessage());

        } finally{
            try {
                if(rs != null) rs.close();
                if(pStmt != null) pStmt.close();

            } catch (SQLException e1) {
                throw new DataCommandException(e1.getMessage());
            }
        }
	}

	/**
	 * The subclass must to define the sql statement to exectue
	 *
	 * @return {@link PreparedStatement}}
	 * @throws SQLException
	 */
	protected abstract PreparedStatement prepareStatement() throws SQLException;


	/**
	 * Assigns the values of fields present in the {@link ResultSet} to the Map.
	 * @param rs
	 * @return a Map<fieldName, fieldValue>
	 * @throws SQLException
	 */
	protected abstract Map<String, Object> getRow(ResultSet rs) throws SQLException;





	public List<Map<String, Object>> getResult() {
		return this.resultList;
	}

}
