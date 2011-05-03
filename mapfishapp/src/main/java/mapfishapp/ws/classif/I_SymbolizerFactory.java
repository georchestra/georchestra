package mapfishapp.ws.classif;

import org.geotools.styling.Symbolizer;

/**
 * Sets the contract for all classes that want to create org.geotools.styling.Symbolizer objects <br />
 * Classes must provides an iterator to access those objects. <br />
 * Useful to build SLD files from scratch
 * @author yoann.buch@gmail.com
 */

public interface I_SymbolizerFactory extends Iterable<Symbolizer> {
}
