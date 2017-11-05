/*
 * Copyright (C) 2009-2017 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
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
