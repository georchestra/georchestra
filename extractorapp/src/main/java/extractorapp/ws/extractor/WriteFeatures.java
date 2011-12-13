package extractorapp.ws.extractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.geotools.data.DataStore;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import extractorapp.ws.extractor.WfsExtractor.GeomType;

/**
 * FeatureVisitor that writes features to shapefiles as they are visited.
 * 
 * One important issue is that shapefiles only support Multiline, Multipoint and MultiPolygon geometries.  So in order to
 * support layers with generic Geometry geometry (can be mixed line, point and polygon) then this class checks the type
 * of geometry and will create new shapefiles as needed.  Each shapefile ends with they geom type.  So one layer
 *  may result in 3 shapefiles:  foo_POINT.shp, foo_LINE.shp and foo_POLY.shp.
 * 
 * Also shapefile attributes are normally case insensitive there cannot be 2 attributes 'attribute' and 'ATTRIBUTE' in
 * the same file.  So in this example the two attributes will be modified so the featuretype will have 'attribute' and
 * 'ATTRIBUTE2' as attributes instead.
 * 
 * @author jeichar
 */
public class WriteFeatures implements FeatureVisitor {

    private static final int                                                    MAX_TRANSACTION_FEATURES = 50000;
    private final SimpleFeatureType                                             _type;
    private final Map<GeomType, FeatureStore<SimpleFeatureType, SimpleFeature>> _dsFiles;
    private final File                                                          _baseDir;
    private final ArrayList<File>                                               _files          = new ArrayList<File>();
    private final Map<String, String>                                           _attNameMapping = new HashMap<String, String>();
    private final DatastoreFactory                                              _dsFactory;
    private final Transaction                                                   _transaction;
    private int                                                                 _featuresInTransaction = 0;

    /**
     * @param type the featuretype definition of the layer being read from (source layer)
     * @param baseDir the directory that the shapefiles will be created in
     * @param outputProjection The output projections
     */
    public WriteFeatures (SimpleFeatureType type, File baseDir, 
            CoordinateReferenceSystem outputProjection, DatastoreFactory dsFactory) {
        _dsFactory = dsFactory;
        _transaction = new DefaultTransaction();
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder ();
        builder.setName (type.getName ());
        Set<String> usedAttNames = new HashSet<String> ();
        
        // for MEF first attribute must be geometry attribute so lets find that first.
        addGeometryAttribute(type, outputProjection, builder, usedAttNames);
        addNonGeomAttributes(type, builder, usedAttNames, dsFactory);
        
        _type = builder.buildFeatureType ();
        _dsFiles = new HashMap<GeomType, FeatureStore<SimpleFeatureType, SimpleFeature>>();
        _baseDir = baseDir;
    }

	private void addNonGeomAttributes(SimpleFeatureType type,
			SimpleFeatureTypeBuilder builder, Set<String> usedAttNames, DatastoreFactory dsFactory) {
		for (int i = 0; i < type.getAttributeCount (); i++) {
            AttributeDescriptor desc = type.getDescriptor (i);
            
            if (desc instanceof GeometryDescriptor) {
                continue;
            }

            String attName = desc.getLocalName ();
            String uniqueName = toUniqueName (type, usedAttNames, i, attName);
            AttributeTypeBuilder attBuilder = new AttributeTypeBuilder ();
            attBuilder.init (desc);
            if(dsFactory instanceof MifDatastoreFactory && desc.getType().getBinding().isAssignableFrom(Short.class)) {
            	attBuilder.setBinding(Integer.class);
            }
            builder.add (attBuilder.buildDescriptor (uniqueName));
        }
	}

	private void addGeometryAttribute(SimpleFeatureType type,
			CoordinateReferenceSystem outputProjection,
			SimpleFeatureTypeBuilder builder, Set<String> usedAttNames) {
		for (int i = 0; i < type.getAttributeCount (); i++) {
            AttributeDescriptor desc = type.getDescriptor (i);
            if (desc instanceof GeometryDescriptor) {
            	String attName = desc.getLocalName ();
                String uniqueName = toUniqueName (type, usedAttNames, i, attName);
                
                AttributeTypeBuilder attBuilder = new AttributeTypeBuilder ();
                attBuilder.init (desc);
                if (desc instanceof GeometryDescriptor) {
                    attBuilder.setCRS (outputProjection);
                }
                builder.add (attBuilder.buildDescriptor (uniqueName));
                
                // shapefiles can only have one geometry so skip any
                // geometry descriptor that is not the default
                break;
            }
        }
	}

    @Override
    public void visit (Feature feature) {
        try {
            SimpleFeature simpleFeature = (SimpleFeature)feature;
            Object defaultGeometry = simpleFeature.getDefaultGeometry ();

            GeomType geomType = WfsExtractor.GeomType.lookup (defaultGeometry.getClass ());
            
            FeatureStore<SimpleFeatureType, SimpleFeature> dsFile = getDatastore (geomType, simpleFeature.getFeatureType ().getTypeName ());

            SimpleFeature copy = copyFeature (simpleFeature, dsFile.getSchema ());

            FeatureCollection<SimpleFeatureType, SimpleFeature> collection = FeatureCollections.newCollection ();
            collection.add (copy);
            dsFile.addFeatures (collection);
        } catch (IOException e) {
            throw new RuntimeException (e);
        }
    }
    
    public void close() throws IOException {
        _transaction.commit();
        _transaction.close();
    }
    
    /**
     * Obtain an array (a copy) of all the shapefiles that were created during
     * writing out the features.
     * 
     * The array is a copy so modifying array will have no effect on the internals of this class
     */
    public File[] getShapeFiles () {
        return _files.toArray (new File[_files.size ()]);
    }

    /* -------------------  Private Methods  -------------------------------*/
    
    /* -------------------  Support methods for visit  -------------------------------*/
    private SimpleFeature copyFeature (SimpleFeature simpleFeature, SimpleFeatureType baseType) throws IOException {
        if(_featuresInTransaction>MAX_TRANSACTION_FEATURES){
            _featuresInTransaction = 0;
            _transaction.commit();
        }
        _featuresInTransaction  += 1;
        String id = simpleFeature.getIdentifier ().getID ();
        SimpleFeatureBuilder builder = new SimpleFeatureBuilder (baseType);
        Set<Entry<String, String>> entries = _attNameMapping.entrySet ();
        for (Entry<String, String> entry : entries) {
            Object value = simpleFeature.getAttribute (entry.getKey ());
            builder.set (entry.getValue (),value);
        }
        SimpleFeature copy = builder.buildFeature (id);
        return copy;
    }

    private FeatureStore<SimpleFeatureType, SimpleFeature> getDatastore (GeomType geomType, String baseName) throws IOException {
        FeatureStore<SimpleFeatureType, SimpleFeature> fs = _dsFiles.get (geomType);
        if (fs == null) {
            String newName = _type.getTypeName();
            newName = FileUtils.toSafeFileName(baseName + "_" + geomType);
            File file = new File(_baseDir, newName + "." + _dsFactory.extension());
            for (int i = 1; file.exists(); i++) {
                newName = baseName + "_" + geomType + i;
                file = new File(_baseDir, newName + "." + _dsFactory.extension());
            }
            SimpleFeatureType updatedFeatureType = updateFeatureTypeGeom (newName, geomType);

            DataStore ds = _dsFactory.create(file, updatedFeatureType);
            fs = (FeatureStore<SimpleFeatureType, SimpleFeature>) ds.getFeatureSource(newName);
            fs.setTransaction(_transaction);
            _dsFiles.put (geomType, fs);
            _files.add (file);
        }
        return fs;
    }

    private SimpleFeatureType updateFeatureTypeGeom (String newName, GeomType geomType) {
        SimpleFeatureTypeBuilder ftBuilder = new SimpleFeatureTypeBuilder ();
        ftBuilder.init (_type);
        ftBuilder.setName(newName);
        GeometryDescriptor geometryDescriptor = _type.getGeometryDescriptor ();
        String geomAttName = geometryDescriptor.getLocalName ();
        ftBuilder.remove (geomAttName);
        
        AttributeTypeBuilder attbuilder = new AttributeTypeBuilder();
        attbuilder.init(geometryDescriptor);
        attbuilder.setBinding(geomType.binding);

        // mif needs geom to be first attribute
        ftBuilder.add (0, attbuilder.buildDescriptor(geomAttName));
        
        SimpleFeatureType updatedFeatureType = ftBuilder.buildFeatureType ();
        return updatedFeatureType;
    }

    /* -------------------  Support methods for constructor  -------------------------------*/
    private String toUniqueName (SimpleFeatureType type, Set<String> usedAttNames, int i, String attName) {
        // shapefiles always set the attribute names to uppercase so
        // we have to check that there aren't 2 attributes
        // with names that will conflict
        String uniqueName = attName;
        for (int j = 2; usedAttNames.contains (uniqueName.toUpperCase ())
                || (attName != uniqueName && containsName (type, 0, uniqueName)); j++) {
            uniqueName = attName + j;
        }
        
        usedAttNames.add (uniqueName.toUpperCase ());
        _attNameMapping.put (attName, uniqueName);
        return uniqueName;
    }
    
    private boolean containsName (SimpleFeatureType type, int startIndex, String uniqueName) {
        for (int i = type.getAttributeCount()-1; i > startIndex; i--) {
            String localName = type.getDescriptor (i).getLocalName ();
            if(localName.equalsIgnoreCase (uniqueName)) {
                return true;
            }
        }
        return false;
    }

}
