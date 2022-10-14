package org.georchestra.console.integration.ds;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.dbunit.dataset.datatype.AbstractDataType;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.datatype.TypeCastException;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;

import com.google.common.base.Splitter;

/**
 * Extension of dbunit's {@linkplain PostgresqlDataTypeFactory} that also
 * handles array(text) data types, so table columns for roles can be tested.
 * <p>
 * Usage:
 *
 * <pre>
 * <code>
 * &#64;DBUnit(dataTypeFactoryClass = PostgresExtendedDataTypeFactory.class)
 * &#64;DataSet(value = ....)
 * public @Test void testSomethingWithTextArray() {
 *  ....
 * }
 * </code>
 * </pre>
 */
public class PostgresExtendedDataTypeFactory extends PostgresqlDataTypeFactory {

    public @Override DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
        if (sqlType == Types.ARRAY)
            if ("_text".equals(sqlTypeName)) {
                return new TextArrayType("_text");
            } else if ("_varchar".equals(sqlTypeName)) {
                return new TextArrayType("_varchar");
            }

        return super.createDataType(sqlType, sqlTypeName);
    }

    public static class TextArrayType extends AbstractDataType {

        public TextArrayType(String name) {
            super(name, Types.ARRAY, String.class, false);
        }

        public @Override Array getSqlValue(int column, ResultSet resultSet) throws SQLException, TypeCastException {
            return resultSet.getArray(column);
        }

        public @Override void setSqlValue(Object array, int column, PreparedStatement statement)
                throws SQLException, TypeCastException {

            statement.setArray(column, toSqlArray(array, statement.getConnection()));
        }

        public @Override String[] typeCast(Object value) throws TypeCastException {
            System.err.println(value);
            throw new UnsupportedOperationException();
        }

        private Array toSqlArray(final Object object, Connection connection) throws SQLException, TypeCastException {
            if (object == null) {
                return null;
            }
            if (object instanceof Array) {
                return (Array) object;
            }
            if (object instanceof String) {
                String str = (String) object;
                List<String> list = Splitter.on(',').splitToList(str);
                Object[] value = new Object[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    value[i] = list.get(i);
                }
                Array array = connection.createArrayOf("text", value);
                return array;
            }
            throw new TypeCastException(object, this);
        }

    }
}
