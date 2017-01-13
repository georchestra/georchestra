/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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

package org.georchestra.analytics.util;

import java.io.Serializable;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

public class PostGresArrayStringType implements UserType {
	protected static final int[] SQL_TYPES = { Types.ARRAY };
	@Override
	public final Object deepCopy(final Object value) throws HibernateException {
	    return value;
	}

	@Override
	public final boolean isMutable() {
	    return false;
	}

	@Override
	public final Object assemble(final Serializable arg0, final Object arg1)
	        throws HibernateException {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public final Serializable disassemble(final Object arg0) throws HibernateException {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public final boolean equals(final Object x, final Object y) throws HibernateException {
	    if (x == y) {
	        return true;
	    } else if (x == null || y == null) {
	        return false;
	    } else {
	        return x.equals(y);
	    }
	}

	@Override
	public final int hashCode(final Object x) throws HibernateException {
	    return x.hashCode();
	}

	@Override
	public final Object replace(
	    final Object original,
	    final Object target,
	    final Object owner) throws HibernateException {
	    return original;
	}

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	@Override
	public Class returnedClass() {
		return String[].class;
	}

	@Override
	public final Object nullSafeGet(
	        final ResultSet resultSet, 
	        final String[] names, 
	        final SessionImplementor session, 
	        final Object owner) throws HibernateException, SQLException {
	    if (resultSet.wasNull()) {
	        return null;
	    }

	    String[] array = (String[]) resultSet.getArray(names[0]).getArray();
	    return array;
	}

	@Override
	public final void nullSafeSet(final PreparedStatement statement, final Object value, 
	        final int index, final SessionImplementor session) throws HibernateException, SQLException {

	    if (value == null) {
	        statement.setNull(index, SQL_TYPES[0]);
	    } else {
	        String[] castObject = (String[]) value;
	        Array array = session.connection().createArrayOf("text", castObject);
	        statement.setArray(index, array);
	    }
	}
}
