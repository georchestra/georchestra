package org.georchestra.analytics.util;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The purpose of this class is to keep parameter replacment feature without using SQL prepared statements.
 *
 * Prepared statements implies that SQL plan execution is computed before parameters values are known. So with
 * partitioned table, execution plan is always wrong and all tables are scanned. This lead to very long query.
 * To fix this issue, this class, use a standard Statement instance to use parameter remplacment and then get raw SQL
 * query with toString() method. Then a second method provide a reconnection feature and test db connection before
 * sending real query to the database. If test fails, one reconnection is tried.
 * Another feature of this class, it adds a name on sql parameter: '?' is replaced by parameter name in braces : {\w+}
 *
 * Example :
 *
 * String sql = "SELECT * FROM users WHERE group_name = {group} LIMIT {count}";
 * Map<String, Object> sqlValues = new HashMap<String, Object>();
 * sqlValues.put("group", "admin");
 * sqlValues.put("count", 100);
 *
 * DBConnection db = new DBConnection(jpaDataSource);
 * String rawSql = db.generateQuery(sql, sqlValues);
 * ResultSet res = db.execute(rawSql);
 *
 */
public class DBConnection {

    private DataSource dataSource;
    private Pattern namedParameterPattern;
    private Connection nativeConnection;

    public DBConnection(DataSource jpaDataSource) throws PropertyVetoException, SQLException {

        this.namedParameterPattern = Pattern.compile("\\{(\\w+)\\}");
        this.nativeConnection = jpaDataSource.getConnection();
        this.dataSource = jpaDataSource;
    }

    private void checkConnection() throws SQLException {

        try {
            Statement st = this.nativeConnection.createStatement();
            st.executeQuery("SELECT 1");
        } catch (SQLException e) {
            try{
                this.nativeConnection.close();
            } catch(SQLException ex){}

            // Try to reconnect to DB one time
            this.nativeConnection = this.dataSource.getConnection();
        }
    }

    public String generateQuery(String sql, Map<String, Object> values) throws SQLException {

        // Check connection to database
        this.checkConnection();

        List<String> parameterNames = new LinkedList<String>();

        // Replace named parameter with standard prepared statement parameter : '?'
        Matcher m = this.namedParameterPattern.matcher(sql);
        while(m.find()){
            String parameterName = m.group(1);
            parameterNames.add(parameterName);
            sql = sql.replaceFirst("\\{" + parameterName + "\\}","?");
            m = this.namedParameterPattern.matcher(sql);
        }

        // Create statement
        PreparedStatement st = this.nativeConnection.prepareStatement(sql);

        for(int i=0;i<parameterNames.size();i++){
            // Check if parameter is defined
            if(!values.containsKey(parameterNames.get(i)))
                throw new IllegalArgumentException("No value specified for parameter : "
                        + parameterNames.get(i) + " in " + sql);
            st.setObject(i + 1, values.get(parameterNames.get(i)));
        }
        return st.toString();
    }

    public ResultSet execute(String query) throws SQLException {
        // Check connection to database :
        this.checkConnection();
        return this.nativeConnection.createStatement().executeQuery(query);

    }



}
