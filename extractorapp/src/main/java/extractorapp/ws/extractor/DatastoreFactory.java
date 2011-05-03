package extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;

import org.geotools.data.DataStore;
import org.opengis.feature.simple.SimpleFeatureType;

public interface DatastoreFactory {
    public DataStore create(File filename, SimpleFeatureType schema) throws IOException;

    public String extension();
}
