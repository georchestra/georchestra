package org.georchestra.mapfishapp.ws.classif;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.store.ContentFeatureCollection;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.data.wfs.impl.WFSContentDataStore;
import org.geotools.data.wfs.impl.WFSDataStoreFactory;
import org.mockito.Mockito;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;

public class MockWFSDataStoreFactory extends WFSDataStoreFactory {

	@Override
	public WFSContentDataStore createDataStore(Map arg0) throws IOException {
		WFSContentDataStore mockDs = Mockito.mock(WFSContentDataStore.class);
		SimpleFeatureType mockFType = Mockito.mock(SimpleFeatureType.class);
		ContentFeatureSource mockFeatureSource = Mockito.mock(ContentFeatureSource.class);
		ContentFeatureCollection mockFeatureCollection = Mockito.mock(ContentFeatureCollection.class);
		AttributeType mockAttributeType = Mockito.mock(AttributeType.class);
		SimpleFeatureIterator  mockFeatIterator = new MockFeatureIterator();
		
		// WFSDataStore actions
		Mockito.when(mockDs.getSchema(Mockito.anyString())).thenReturn(mockFType);
		Mockito.when(mockDs.getFeatureSource(Mockito.anyString())).thenReturn(mockFeatureSource);

		// SimpleFeatureType actions
		Mockito.when(mockFType.indexOf(Mockito.anyString())).thenReturn(0);
		Mockito.when(mockFType.getType(Mockito.anyString())).thenReturn(mockAttributeType);

		// SimpleFeatureCollection actions
		Mockito.when(mockFeatureCollection.features()).thenReturn(mockFeatIterator);
		
		// SimpleFetureSource actions
		Mockito.when(mockFeatureSource.getFeatures()).thenReturn(mockFeatureCollection);

		// AttributeType actions
		Mockito.when(mockAttributeType.getBinding()).thenReturn((Class) Integer.class);

		return mockDs; 
	}
    private class MockFeatureIterator implements SimpleFeatureIterator {
    	private int count = 0;
    	
		@Override
		public void close() {}

		@Override
		public boolean hasNext() {
			count ++;
			if (count > 10)
				return false;
			return true;
		}

		@Override
		public SimpleFeature next() throws NoSuchElementException {
			SimpleFeature feat = Mockito.mock(SimpleFeature.class);
			Property prop = Mockito.mock(Property.class);
			Mockito.when(feat.getID()).thenReturn(String.format("feature%d", count));
			Mockito.when(prop.getValue()).thenReturn((Object) new Double(count));
			Mockito.when(feat.getFeatureType()).thenReturn(Mockito.mock(SimpleFeatureType.class));
			Mockito.when(feat.getProperty(Mockito.anyString())).thenReturn(prop);
			return feat;
		}
    	
    }
  

}
