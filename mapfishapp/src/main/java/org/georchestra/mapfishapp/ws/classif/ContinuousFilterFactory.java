/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;

/**
 * Provides Filter objects from continuous values. These SLD filters corresponds to the PropertyIsBetween tags 
 * from the SLD specifications. It can be therefore added to any Rule. 
 * @author yoann.buch@gmail.com
 *
 */
public class ContinuousFilterFactory implements I_FilterFactory {

    private ArrayList<Interval> _intervals = new ArrayList<Interval>();
    private String _propertyName;
    
    /**
     * Trivial class to store an interval (one left and one right value)
     */
    public class Interval {
        private double _left;
        private double _right;
        
        public Interval(double left, double right) { 
            if(right < left) {
                throw new IllegalArgumentException("right cannot be lesser than left");
            }     
            _left = left;
            _right = right;
        }
        
        public double getRight() {
            return _right;
        }
        
        public double getLeft() {
            return _left;
        }
        
        public String toString() {
            return "[" + _left + ";" + _right + "]";
        }
    }
    
    /**
     * Provides a way to access Filter objects that are created on the fly from the intervals found by the classification
     */
    public class InternalIterator implements Iterator<Filter> {

        private Iterator<Interval> _it;
        
        public InternalIterator() {
            _it = _intervals.iterator();
        }
        
        /**
         * Does it have any Filter left?
         */
        public boolean hasNext() {
            return _it.hasNext();
        }

        /**
         * Gets next Filter object
         */
        public Filter next() {
            // Considered by SLD specification as inclusive intervals
            Interval interval = _it.next();
            double lowerBoundary = interval.getLeft();
            double upperBoundary = interval.getRight();
            
            FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2(null);
            org.opengis.filter.Filter _filter = filterFactory.between(
                filterFactory.property(_propertyName), 
                filterFactory.literal(lowerBoundary),
                filterFactory.literal(upperBoundary));
            Filter filter = new Filter(_filter, "entre " + lowerBoundary + " et " + upperBoundary);
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
     * Classifies values given the number of classes. The intervals found by this classification 
     * will be used to generate Filter objects. <br />
     * For n classes, there are n {@link Interval} objects
     * @param values values to classify
     * @param classCount number of classes
     * @param propertyName property name corresponding to the WFS request
     */
    public ContinuousFilterFactory(final ArrayList<Double> values, final int classCount, final String propertyName) {
        
        if(values == null) {
            throw new IllegalArgumentException("values cannot be null");
        }
        if(values.size() == 0) {
            throw new IllegalArgumentException("values cannot be empty");
        }
        
        _propertyName = propertyName;
        
        // classify
        doQuantile(values, classCount);
    }
    
    /**
     * Execute Quantile classification on the values. Store computed intervals for further use. <br />
     * Values that are the same MUST belong to the same class
     * @param values values to classify
     * @param classCount number of classes
     */
    private void doQuantile(ArrayList<Double> values, int classCount) {
        //sort values
        Collections.sort(values);
        
        // get number of values that a class should have in average
        double dIntervalSize = (double) values.size() / classCount;
        int intervalSize = (int) Math.ceil(dIntervalSize); // ceil value to be sure to include all the values
        
        // assign values to different intervals
        int currentIndex = 0;
        while(currentIndex < values.size()) {
            int leftIndex = currentIndex;
            int rightIndex;
            if(currentIndex + intervalSize < values.size() ) {
                // class must contain as many values as space available in a class
                rightIndex = currentIndex + intervalSize -1;
                
                // add any further value that is equal to the last value
                while((rightIndex+1 < values.size()) &&
                        (values.get(rightIndex).equals(values.get(rightIndex+1)))) {
                    // even though the value should belong to the next class, it is added because
                    // the same value is already present in this one
                    rightIndex++;
                }
            }
            else {
                // less values than space available in a class
                // let's add the rest
                rightIndex = values.size() - 1;
            }       
            
            // store this interval
            Interval interval = new Interval(values.get(leftIndex), values.get(rightIndex));
            _intervals.add(interval);

            // push cursor further away along the values
            currentIndex = rightIndex + 1;
        }    
    }

    /**
     * Gives an iterator to access Filter objects
     */
    public Iterator<Filter> iterator() {
        return new InternalIterator();
    }
    
    /**
     * Gets the interpolated boundaries
     * @return List of interpolated boundaries
     */
    public ArrayList<Interval> getIntervals() {
        return _intervals;
    }

}
