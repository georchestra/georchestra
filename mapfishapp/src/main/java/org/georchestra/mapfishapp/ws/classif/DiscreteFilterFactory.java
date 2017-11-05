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

package org.georchestra.mapfishapp.ws.classif;

import java.util.Iterator;
import java.util.Set;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;

/**
 * Provides Filter objects from discrete values. These SLD filters corresponds to the PropertyIsEqualTo tags 
 * from the SLD specifications. It can be therefore added to any Rule. 
 * @author yoann.buch@gmail.com
 *
 */
public class DiscreteFilterFactory implements I_FilterFactory {

    private Set<String> _values;
    private String _propertyName;
    
    /**
     * Provides access to the created Filter objects
     */
    public class InternalIterator implements Iterator<Filter> {

        private Iterator<String> _it;
        
        public InternalIterator() {
            _it = _values.iterator();
        }
        
        /**
         * Determines if another Filter object exists
         */
        public boolean hasNext() {
            return _it.hasNext();
        }

        /**
         * Gets the next Filter Object
         */
        public Filter next() {
            FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2(null);
            String value = _it.next();
            org.opengis.filter.Filter _filter = filterFactory.equals(
                filterFactory.property(_propertyName), 
                filterFactory.literal(value));
            Filter filter = new Filter(_filter, new String(value)); 
            return filter;
        }

        /**
         * Unsupported
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }  
    }
    
    /**
     * Creates Filters objects from the given values. Those objects are then accessible via 
     * {@link DiscreteFilterFactory#iterator()}
     * @param values one value = one filter
     * @param propertyName property name on which the WFS request is made
     */
    public DiscreteFilterFactory(final Set<String> values, final String propertyName) {
        _values = values;
        _propertyName = propertyName;
    }
    
    /**
     * Provides an iterator to go through all created Filter objects
     */
    public Iterator<Filter> iterator() {
        return new InternalIterator();
    }

}
