package org.georchestra.mapfishapp.ws.classif;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import org.georchestra.mapfishapp.ws.classif.PolygonSymbolizerFactory;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Symbolizer;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests DiscreteFilterFactory
 * @author yoann.buch@gmail.com
 *
 */
public class PolygonSymbolizerFactoryTest {

    private PolygonSymbolizerFactory _interpolationFact;
    private int _classCount = 3;
    private Color _firstColor = Color.RED;
    private Color _lastColor = Color.BLUE;
    
    @Before
    public void setUp() {
        _interpolationFact = new PolygonSymbolizerFactory(_classCount, _firstColor, _lastColor);
    }
    
    /**
     * Should create as many colors as classes
     */
    @Test
    public void testColorCount() {
        Iterator<Symbolizer> it = _interpolationFact.iterator();
        int count = 0;
        while(it.hasNext()) {
            count++;
            it.next();
        }
        
        assertEquals(_classCount, count);
    }
    
    /**
     * Check colors' values in Symbol
     */
    @Test
    public void testColors() {
        ArrayList<Color> colors = _interpolationFact.getColors();
        assertEquals(new Color(255, 0, 0), colors.get(0));
        assertEquals(new Color(127, 0, 127), colors.get(1));
        assertEquals(new Color(0, 0, 255), colors.get(2));
    }
    
    /**
     * Check colors' values in Symbol
     */
    @Test
    public void testColorInSymbol() {
        Iterator<Symbolizer> it = _interpolationFact.iterator();
        PolygonSymbolizer s1 = ((PolygonSymbolizer) it.next()); // first val
        PolygonSymbolizer s2 = ((PolygonSymbolizer) it.next()); // interpolated val
        PolygonSymbolizer s3 = ((PolygonSymbolizer) it.next()); // last val
        
        assertEquals("#FF0000", s1.getFill().getColor().toString());
        assertEquals("#7F007F", s2.getFill().getColor().toString());
        assertEquals("#0000FF", s3.getFill().getColor().toString());
    }
    
    /**
     * Tests constructor with palette id. Should use one palette and use random coloring
     */
    @Test
    public void testPaletteFact() {
        int paletteID = 1;
        int colorCount = 50; // force random coloring
        PolygonSymbolizerFactory fact = new PolygonSymbolizerFactory(paletteID, colorCount);
        
        Iterator<Symbolizer> it  = fact.iterator();
        int count = 0;
        while(it.hasNext()) {
            count++;
            it.next();
        }
        assertEquals(colorCount, count);
        
    }
    
    /**
     * Test reject with unknown palette id
     */
    @Test(expected=IllegalArgumentException.class)
    public void testUnknownPaletteID() {
        @SuppressWarnings("unused")
        PolygonSymbolizerFactory fact = new PolygonSymbolizerFactory(9999999, 1);
    }
    
}
