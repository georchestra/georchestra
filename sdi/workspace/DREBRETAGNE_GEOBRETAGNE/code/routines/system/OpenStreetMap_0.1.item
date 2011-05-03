// ============================================================================
//
// Copyright (C) 2009
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the  agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//   
// ============================================================================
package routines;

/**
 * 
 * @author fxprunarye
 */
public class OpenStreetMap {
	private static String TAGS_SEPARATOR = ";";
	private static String TAGVALUE_SEPARATOR = ":";

	/**
	 * GETTAG( ) Returns the tag value
	 * 
	 * {talendTypes} string | String
	 * 
	 * {Category} OpenStreetMap
	 * 
	 * {param} String(null)
	 * 
	 * {param} String(null)
	 * 
	 * {example} GETTAG(row1.tags, "name")
	 * 
	 */
	public static String GETTAGVALUE(String tags, String tagName) {

		int idx = tags.indexOf(tagName + TAGVALUE_SEPARATOR);
		if (idx == -1)
			return " ";

		String value = tags.substring(idx + TAGVALUE_SEPARATOR.length()
				+ tagName.length());
		idx = value.indexOf(TAGS_SEPARATOR);

		if (idx == -1)
			return value;
		else
			return value.substring(0, idx);
	}
}