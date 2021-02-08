package org.georchestra.datafeeder.service.publish.impl;

import java.util.Optional;

import org.geoserver.openapi.model.catalog.FeatureTypeInfo;
import org.geoserver.openapi.model.catalog.WorkspaceInfo;
import org.geoserver.openapi.v1.model.Layer;

import lombok.NonNull;

public class GeoServerRemoteService {

	public Optional<Layer> findLayerByName(@NonNull String workspace, @NonNull String proposedName) {
		throw new UnsupportedOperationException();
	}

	public WorkspaceInfo getOrCreateWorkspace(String workspaceName) {
		throw new UnsupportedOperationException();
	}

	public void create(FeatureTypeInfo fti) {
		// TODO Auto-generated method stub
		
	}

	public void update(FeatureTypeInfo fti) {
		// TODO Auto-generated method stub
		
	}

	public FeatureTypeInfo getFeatureTypeInfo(String publishedWorkspace, String publishedName) {
		// TODO Auto-generated method stub
		return null;
	}

}
