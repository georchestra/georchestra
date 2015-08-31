package org.georchestra.mapfishapp.ws.classif;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

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

        // Note: Java 8 does not behave the same way as the previous versions
        // when building a set from a list, if we consider the following list:
        // "1,2,1,3,4,4,5", building a set will provide the following:
        // Java < 8: "3,4,1,2,5"
        // Java   8: "1,2,3,4,5"
        //
        // Instead of using a HashSet, we are going to use a LinkedHashSet, to
        // have a coherent element order across versions.

        Set<String> values = new LinkedHashSet<String>(Arrays.asList(new String[]
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
        assertEquals(_propertyName, it.next().getGISFilter().accept(propertyNameVisitor, null));
        assertEquals(_propertyName, it.next().getGISFilter().accept(propertyNameVisitor, null));
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

        assertEquals("val1", it.next().getGISFilter().accept(propertyNameVisitor, null));
        assertEquals("val2", it.next().getGISFilter().accept(propertyNameVisitor, null));
    }
}
