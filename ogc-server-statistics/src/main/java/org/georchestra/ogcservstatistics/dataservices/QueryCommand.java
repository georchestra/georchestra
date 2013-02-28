/**
 * 
 */
package org.georchestra.ogcservstatistics.dataservices;

import java.util.List;
import java.util.Map;

/**
 * @author Mauricio Pazos
 *
 */
public interface QueryCommand extends DataCommand{
	

	/**
	 * Results of query execution
	 * @return  List of pairs fieldName, fieldValue. 
	 */
	public List<Map<String,Object>> getResult();

	/**
	 * @param year required parameter
	 */
	public void setYear(int year);

	/**
	 * 
	 * @param month optional parameter
	 */
	public void setMonth(int month);

	public void setLimit(int limit);
}
