/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
 * 
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.feature;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.geotools.filter.CompareFilter;
import org.geotools.filter.FilterType;
import org.geotools.filter.Filters;
import org.geotools.filter.LengthFunction;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LogicFilter;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;


/**
 * <p>
 * Proposal: AttributeType utilities class.
 * </p>
 * @author Luca S. Percich, AMA-MI
 * @source $URL: http://svn.osgeo.org/geotools/branches/2.5.x/modules/unsupported/mif/src/main/java/org/geotools/feature/AttributeTypes.java $
 */
public class AttributeTypes {
    // Returned by getFieldLength() functions when field length is undefined
    public static final int FIELD_LENGTH_UNDEFINED = 0;

    /**
     * <p>
     * Returns the field length defined via a restrinction for an attribute
     * type
     * </p>
     * 
     * <p>
     * This should be considered the maximum allowed length for the given
     * attribute, which means:
     * 
     * <ul>
     * <li>
     * the max number of chars in a <b>string</b>
     * </li>
     * <li>
     * the maximum precision for a <b>float</b>
     * </li>
     * <li>
     * no meaning for <b>Integer</b>, <b>Boolean</b>, <b>Date</b>?
     * </li>
     * </ul>
     * </p>
     *
     * @param attr The attribute type
     * @param defaultLength The default field length
     *
     * @return The defined field length, or defaultLength if no maximum length
     *         has been defined.
     */
    public static int getFieldLength(AttributeDescriptor attr, int defaultLength) {
        int length = getFieldLengthFromFilter(attr.getType().getRestrictions());

        if (length == FIELD_LENGTH_UNDEFINED) {
            length = defaultLength;
        }

        return length;
    }

    /**
     * <p>
     * Returns the field length defined via a restrinction for an attribute
     * type
     * </p>
     *
     * @param attr
     *
     * @return the defined field length, or FIELD_LENGTH_UNDEFINED if no
     *         maximum length has been defined.
     */
    public static int getFieldLength(AttributeDescriptor attr) {
        return getFieldLength(attr, FIELD_LENGTH_UNDEFINED);
    }

    /**
     * <p>
     * Obtains a field length from a filter possibly containing a
     * LengthFunction expression
     * </p>
     *
     * @param filter the given filter
     *
     * @return The maximum field length found in the filter, or
     *         FIELD_LENGTH_UNDEFINED if none was found;
     */
    public static int getFieldLengthFromFilter(List<Filter> filterList) {
		int length = FIELD_LENGTH_UNDEFINED;

		for (Filter filter : filterList) {
			if ((filter != null) && (filter != Filter.EXCLUDE)
					&& (filter != Filter.INCLUDE)) {
				short filterType = Filters.getFilterType(filter);

				if ((filterType == FilterType.COMPARE_LESS_THAN)
						|| (filterType == FilterType.COMPARE_LESS_THAN_EQUAL)
						|| (filterType == FilterType.COMPARE_EQUALS)) {
					try {
						CompareFilter cf = (CompareFilter) filter;

						if (cf.getLeftValue() instanceof LengthFunction) {
							length = Integer.parseInt(((LiteralExpression) cf
									.getRightValue()).getLiteral().toString());
						} else {
							if (cf.getRightValue() instanceof LengthFunction) {
								length = Integer
										.parseInt(((LiteralExpression) cf
												.getLeftValue()).getLiteral()
												.toString());
							}
						}

						if (filterType == FilterType.COMPARE_LESS_THAN) {
							length--;
						}
					} catch (NumberFormatException e) {
						continue; // try the next thing
					}

					// In case of a complex filter, looks for the maximum defined length in filter
				} else if ((filterType == FilterType.LOGIC_AND)
						|| (filterType == FilterType.LOGIC_OR)) {
					for (Iterator it = ((LogicFilter) filter)
							.getFilterIterator(); it.hasNext();) {
						Filter subFilter = (Filter) it.next();
						int subLength = getFieldLengthFromFilter(Arrays.asList(new Filter[]{subFilter}));

						if (subLength > length) {
							length = subLength;
						}
					}
				}
			}
		}
		return length;
	}
}
