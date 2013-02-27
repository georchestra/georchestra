package org.georchestra.mapfishapp.ws.classif;

/**
 * Sets the contract for all classes that want to create org.opengis.filter.Filter objects <br />
 * Classes must provides an iterator to access those objects. <br />
 * Useful to build SLD files from scratch
 * @author yoann.buch@gmail.com
 *
 */
public interface I_FilterFactory extends Iterable<Filter> {

}
