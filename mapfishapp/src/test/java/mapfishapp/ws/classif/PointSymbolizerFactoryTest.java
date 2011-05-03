package mapfishapp.ws.classif;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import mapfishapp.ws.classif.PointSymbolizerFactory;

import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.junit.Test;

/**
 * Test PointSymbolizerFactory
 * @author yoann.buch@gmail.com
 *
 */

public class PointSymbolizerFactoryTest {

    /**
     * Checks that the number of symbols must be equal to the number of classes
     */
    @Test
    public void testSymbolCount() {

        int classCount = 5;
        PointSymbolizerFactory psf = new PointSymbolizerFactory(classCount, 2, 16);
        Iterator<Symbolizer> symbolizers = psf.iterator();
        
        int count = 0;
        while(symbolizers.hasNext()) {
            symbolizers.next();
            count++;
        }
        
        assertEquals(classCount, count);  
        assertEquals(classCount, psf.getSizes().size());
    }
    
    /**
     * Checks boundaries case: 1 class = 1 symbolizer
     */
    @Test
    public void testSymbolCountOne() {

        int classCount = 1;
        PointSymbolizerFactory psf = new PointSymbolizerFactory(classCount, 2, 16);
        Iterator<Symbolizer> symbolizers = psf.iterator();
        
        int count = 0;
        while(symbolizers.hasNext()) {
            symbolizers.next();
            count++;
        }
        
        assertEquals(classCount, count);  
        assertEquals(classCount, psf.getSizes().size());
    }
    
    /**
     * Checks reject when negative class count is provided 
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNegativeClassCount() {
        @SuppressWarnings("unused")
        PointSymbolizerFactory psf = new PointSymbolizerFactory(-1, 2, 16);
    }
   
    /**
     * Checks reject when 0 class count is provided 
     */
    @Test(expected=IllegalArgumentException.class)
    public void testZeroClassCount() {
        @SuppressWarnings("unused")
        PointSymbolizerFactory psf = new PointSymbolizerFactory(0, 2, 16);
    }
    
    /**
     * Checks reject when negative min size
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNegativeMinSize() {
        @SuppressWarnings("unused")
        PointSymbolizerFactory psf = new PointSymbolizerFactory(2, -2, 16);
    }
    
    /**
     * Checks reject when negative max size
     */
    @Test(expected=IllegalArgumentException.class)
    public void testNegativeMaxSize() {
        @SuppressWarnings("unused")
        PointSymbolizerFactory psf = new PointSymbolizerFactory(2, 2, -16);
    }
    
    /**
     * Checks reject when max and min sizes are the same
     */
    @Test(expected=IllegalArgumentException.class)
    public void testSameMinMaxSize() {
        @SuppressWarnings("unused")
        PointSymbolizerFactory psf = new PointSymbolizerFactory(2, 4, 4);
    }
    
    /**
     * Checks that generated symbolizers contain the right sizes
     */
    @Test
    public void testMaxAndMinValues() {
        
        int classCount = 3;
        PointSymbolizerFactory psf = new PointSymbolizerFactory(classCount, 2, 16);
        Iterator<Symbolizer> symbolizers = psf.iterator();

        PointSymbolizer ps = (PointSymbolizer) symbolizers.next();
        assertEquals("2", ps.getGraphic().getSize().toString());
        
        ps = (PointSymbolizer) symbolizers.next(); 
        assertEquals("9", ps.getGraphic().getSize().toString());
        
        ps = (PointSymbolizer) symbolizers.next();
        assertEquals("16", ps.getGraphic().getSize().toString());

    }
    
    /**
     * Check symbol characteristics: color, opacity and shape name
     */
    @Test
    public void testSymbolCharacs() {
        
        int classCount = 3;
        PointSymbolizerFactory psf = new PointSymbolizerFactory(classCount, 2, 16);
        psf.setSymbol(StyleBuilder.MARK_STAR);
        psf.setOpacity(0.2);
        psf.setColor(Color.GREEN);
        Iterator<Symbolizer> symbolizers = psf.iterator();

        PointSymbolizer ps = (PointSymbolizer) symbolizers.next();
        Mark[] marks = ps.getGraphic().getMarks();
        assertEquals(StyleBuilder.MARK_STAR, marks[0].getWellKnownName().toString()); // check shape name
        assertEquals("0.2", ps.getGraphic().getOpacity().toString()); // check opacity
        assertEquals("#00FF00", marks[0].getFill().getColor().toString()); // check color
    }
    
    /**
     * Check interpolated sizes
     */
    @Test
    public void testInterpolatedSizes() {
        int classCount = 5;
        PointSymbolizerFactory psf = new PointSymbolizerFactory(classCount, 4, 30);
        ArrayList<Integer> sizes = psf.getSizes();
        
        assertEquals(classCount, sizes.size());
        
        assertEquals(4, (int) sizes.get(0));
        assertEquals(10, (int) sizes.get(1));
        assertEquals(16, (int) sizes.get(2));
        assertEquals(22, (int) sizes.get(3));
        assertEquals(28, (int) sizes.get(4));
    }
    
    
    
}
