/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

package org.georchestra.ogcservstatistics.dataservices;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Maintains the abstract behavior required to execute a SQL query. The subclass
 * must implement the methods:
 * 
 * <pre>
 * prepareStatement() 
 * getRow()
 * </pre>
 * 
 * @author Mauricio Pazos
 */
public abstract class AbstractQueryCommand extends AbstractDataCommand implements QueryCommand {

	private LinkedList<Map<String, Object>> resultList;

	protected int year = -1;
	protected int month = -1;
	protected int limit = -1;

	@Override
	public void setYear(int year) {

		assert year > 0 : "year is expected";
		this.year = year;
	}

	@Override
	public void setMonth(int month) {
		assert (month >= 1) && (month <= 12) : "month must be a value between 1 and 12";
		this.month = month;
	}

	@Override
	public void setLimit(int limit) {
		assert limit > 1 : "limit must be greather than 1";

		this.limit = limit;
	}

	/**
	 * This template method executes the sql statement specified in the
	 * prepareStatement method.
	 * 
	 * @see org.georchestra.ogcservstatistics.dataservices.DataCommand#execute()
	 */
	@Override
	public void execute() throws DataCommandException {

		assert this.connection != null : "database connection is null, use setConnection";

		// executes the sql statement and populates the list with the data present in
		// the result set
		ResultSet rs = null;
		PreparedStatement pStmt = null;
		try {
			pStmt = prepareStatement();

			rs = pStmt.executeQuery();

			this.resultList = new LinkedList<Map<String, Object>>();

			while (rs.next()) {
				this.resultList.add(getRow(rs));
			}

		} catch (SQLException e) {

			throw new DataCommandException(e.getMessage());

		} finally {
			try {
				if (rs != null)
					rs.close();
				if (pStmt != null)
					pStmt.close();

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
	 * 
	 * @param rs
	 * @return a Map<fieldName, fieldValue>
	 * @throws SQLException
	 */
	protected abstract Map<String, Object> getRow(ResultSet rs) throws SQLException;

	/**
	 * @see org.georchestra.ogcservstatistics.dataservices.QueryCommand#getResult()
	 */
	@Override
	public List<Map<String, Object>> getResult() {
		return this.resultList;
	}

}
