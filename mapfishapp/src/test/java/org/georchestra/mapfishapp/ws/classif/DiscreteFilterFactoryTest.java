package org.georchestra.mapfishapp.ws.classif;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.georchestra.mapfishapp.ws.classif.DiscreteFilterFactory;
import org.georchestra.mapfishapp.ws.classif.Filter;
import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.junit.Before;
import org.junit.Test;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.PropertyIsEqualTo;

/**
 * Tests DiscreteFilterFactory
 * @author yoann.buch@gmail.com
 *
 */
public class DiscreteFilterFactoryTest {

    private DiscreteFilterFactory _fact;
    private String _propertyName = "foo";
    private int _valuesCount;
    
    @Before
    public void beforeEachTest() {
        // same values can be encountered many times
        Set<String> values = new HashSet<String>(Arrays.asList(new String[] 
                      {"val1", "val2", "val1", "val3", "val4", "val4", "val5"}
                    ));
        _valuesCount = values.size();
        _fact = new DiscreteFilterFactory(values, _propertyName);
    }
    
    /**
     * Finds as many Filter objects as many values
     */
    @Test
    public void testValuesCount() {
        Iterator<Filter> it = _fact.iterator();
        int count=0;
        while(it.hasNext()) {
            it.next();
            count++;
        }
        assertEquals(_valuesCount, count);
    }
    
    /**
     * Checks property name in Filter
     */
    @Test
    public void testPropertyName() {
        Iterator<Filter> it = _fact.iterator();
        
        FilterVisitor propertyNameVisitor = new DefaultFilterVisitor(){
            public Object visit( PropertyIsEqualTo filter, Object data ) {
                return filter.getExpression1().toString();
            }
        };
        // tests 2 values
        assertEquals(_propertyName, (String) it.next().getGISFilter().accept(propertyNameVisitor, null));
        assertEquals(_propertyName, (String) it.next().getGISFilter().accept(propertyNameVisitor, null));
    }
    
    /**
     * Checks value in Filter
     */
    @Test
    public void testValue() {
        Iterator<Filter> it = _fact.iterator();
        
        FilterVisitor propertyNameVisitor = new DefaultFilterVisitor(){
            public Object visit( PropertyIsEqualTo filter, Object data ) {
                return filter.getExpression2().toString();
            }
        };
        
        // tests 2 values
        // Unique values are stored in a HashMap => values are ordered differently
        assertEquals("val3", (String) it.next().getGISFilter().accept(propertyNameVisitor, null));
        assertEquals("val4", (String) it.next().getGISFilter().accept(propertyNameVisitor, null));
    }
}
