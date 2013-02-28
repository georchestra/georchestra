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
