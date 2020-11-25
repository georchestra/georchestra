package org.georchestra.commons.logging;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Random;

public class Log4j1SamplingFilter extends Filter {
    private final Random rnd = new Random();
    private int threshold = 20; // if threshold == 100
                                // every events are discarded

    @Override
    public int decide(LoggingEvent event) {
        int val = rnd.nextInt(100);
        if (val <= threshold) {
            return Filter.DENY;
        }
        return Filter.NEUTRAL;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }
}
