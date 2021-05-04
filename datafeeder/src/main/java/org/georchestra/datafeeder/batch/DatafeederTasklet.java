package org.georchestra.datafeeder.batch;

import org.springframework.batch.core.step.tasklet.StoppableTasklet;

/**
 * Marker interface for easily find out all datafeeder tasklets and force them
 * to be {@link StoppableTasklet}s
 */
public interface DatafeederTasklet extends StoppableTasklet {

}
