package org.georchestra.mapfishapp.ws.classif;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.georchestra.mapfishapp.ws.classif.ContinuousFilterFactory;
import org.georchestra.mapfishapp.ws.classif.Filter;
import org.georchestra.mapfishapp.ws.classif.ContinuousFilterFactory.Interval;
import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.junit.Test;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.PropertyIsBetween;

/**
 * Tests ContinuousFilterFactory
 * 
 * @author yoann.buch@gmail.com
 *
 */

public class ContinuousFilterFactoryTest {

    /**
     * Checks that created intervals have to be as many as classes
     */
    @Test
    public void testIntervalCount() {

        int classCount = 3;
        ArrayList<Double> values = new ArrayList<Double>(Arrays
                .asList(new Double[] { 2.0, 7.0, 6.0, 6.0, 3.0, -2.0, 0.0, 34.0, 1.0, -3.0, -6.0, 3.0, 10.0, 6.0 }));
        ContinuousFilterFactory cff = new ContinuousFilterFactory(values, classCount, "foo");

        // check intervals count
        assertEquals(classCount, cff.getIntervals().size());
    }

    /**
     * Tests left and right values of calculated intervals. <br />
     * Values that are the same MUST belong to the same class
     */
    @Test
    public void testIntervals() {
        int classCount = 3;
        ArrayList<Double> values = new ArrayList<Double>(Arrays
                .asList(new Double[] { 2.0, 1.0, -3.0, -6.0, 3.0, 10.0, 6.0, 7.0, 6.0, 6.0, 3.0, -2.0, 0.0, 34.0 }));
        ContinuousFilterFactory cff = new ContinuousFilterFactory(values, classCount, "foo");

        // check intervals
        Interval firstInter = cff.getIntervals().get(0);
        assertEquals(-6.0, firstInter.getLeft(), 0);
        assertEquals(1.0, firstInter.getRight(), 0);

        Interval secondInter = cff.getIntervals().get(1);
        assertEquals(2.0, secondInter.getLeft(), 0);
        assertEquals(6.0, secondInter.getRight(), 0);

        Interval thirdInter = cff.getIntervals().get(2);
        // is not 6.0 because we had three 6.0s that had to belong to the same class
        assertEquals(7.0, thirdInter.getLeft(), 0);
        assertEquals(34.0, thirdInter.getRight(), 0);
    }

    /**
     * Acceptance with a single value
     */
    @Test
    public void testSingleValue() {
        int classCount = 3;
        ArrayList<Double> values = new ArrayList<Double>(Arrays.asList(new Double[] { 2.0 }));
        ContinuousFilterFactory cff = new ContinuousFilterFactory(values, classCount, "foo");

        assertEquals(1, cff.getIntervals().size());
        assertEquals(2.0, cff.getIntervals().get(0).getLeft(), 0);
        assertEquals(2.0, cff.getIntervals().get(0).getLeft(), 0);
    }

    /**
     * Checks reject when 0 values is given
     */
    @Test(expected = IllegalArgumentException.class)
    public void testZeroValues() {
        @SuppressWarnings("unused")
        ContinuousFilterFactory cff = new ContinuousFilterFactory(new ArrayList<Double>(), 3, "foo");
    }

    /**
     * Checks reject when values is null
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNullValues() {
        @SuppressWarnings("unused")
        ContinuousFilterFactory cff = new ContinuousFilterFactory(null, 3, "foo");
    }

    /**
     * Should have as many Filter objects as classes
     */
    @Test
    public void testFilterCount() {
        int classCount = 2;
        ArrayList<Double> values = new ArrayList<Double>(Arrays.asList(new Double[] { 12.0, 4.0, -5.0, 3.0 }));
        ContinuousFilterFactory cff = new ContinuousFilterFactory(values, classCount, "foo");

        Iterator<Filter> filters = cff.iterator();
        int count = 0;
        while (filters.hasNext()) {
            filters.next();
            count++;
        }

        assertEquals(2, count);
    }

    /**
     * Verifies filter characteristics: lower and upper boundaries, property name
     */
    @Test
    public void testFilterCharacs() {
        int classCount = 2;
        String propertyName = "foo";
        ArrayList<Double> values = new ArrayList<Double>(Arrays.asList(new Double[] { 12.0, 4.0, -5.0, 3.0 }));
        ContinuousFilterFactory cff = new ContinuousFilterFactory(values, classCount, propertyName);
        Iterator<Filter> filters = cff.iterator();
        Filter filter1 = filters.next();
        Filter filter2 = filters.next();

        // lower boundary
        FilterVisitor lowerBoundaryVisitor = new DefaultFilterVisitor() {
            public Object visit(PropertyIsBetween filter, Object data) {
                return filter.getLowerBoundary().toString();
            }
        };
        assertEquals("-5.0", (String) filter1.getGISFilter().accept(lowerBoundaryVisitor, null));
        assertEquals("4.0", (String) filter2.getGISFilter().accept(lowerBoundaryVisitor, null));

        // upper boundary
        FilterVisitor upperBoundaryVisitor = new DefaultFilterVisitor() {
            public Object visit(PropertyIsBetween filter, Object data) {
                return filter.getUpperBoundary().toString();
            }
        };
        assertEquals("3.0", (String) filter1.getGISFilter().accept(upperBoundaryVisitor, null));
        assertEquals("12.0", (String) filter2.getGISFilter().accept(upperBoundaryVisitor, null));

        // property name
        FilterVisitor nameVisitor = new DefaultFilterVisitor() {
            public Object visit(PropertyIsBetween filter, Object data) {
                return filter.getExpression().toString();
            }
        };
        assertEquals(propertyName, (String) filter1.getGISFilter().accept(nameVisitor, null));
        assertEquals(propertyName, (String) filter2.getGISFilter().accept(nameVisitor, null));
    }
}
