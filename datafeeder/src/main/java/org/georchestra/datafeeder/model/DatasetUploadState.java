/*
 * Copyright (C) 2020 by the geOrchestra PSC
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
package org.georchestra.datafeeder.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "dataset")
public class DatasetUploadState {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private DataUploadJob job;

    @Column
    private String absolutePath;

    @Column
    private String fileName;

    @Column
    private String name;

    @Column
    private UploadStatus status;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    private String error;

    @Column
    private Integer featureCount;

    @Lob
    @Basic(fetch = FetchType.EAGER)
    @Column(length = 1024 * 1024)
    private String sampleGeometryWKT;

    @Embedded
    @AttributeOverrides({ //
            @AttributeOverride(name = "crs_srs", column = @Column(name = "bbox_srs")), //
            @AttributeOverride(name = "crs_wkt", column = @Column(name = "bbox_wkt"))//
    })
    private BoundingBoxMetadata nativeBounds;

    private String encoding;

    @ElementCollection
    @CollectionTable(name = "dataset_sample_properties")
    private List<SampleProperty> sampleProperties = new ArrayList<>();
}
