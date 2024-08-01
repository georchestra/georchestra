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
import java.util.Map;

import javax.persistence.*;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import org.georchestra.datafeeder.service.DataSourceMetadata;

@Data
@EqualsAndHashCode(exclude = { "job" })
@ToString(exclude = "job")
@Entity
@Table(name = "dataset")
public class DatasetUploadState {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private DataUploadJob job;

    @Column(nullable = false, length = 2048)
    private String absolutePath;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private @NonNull JobStatus analyzeStatus = JobStatus.PENDING;

    @Column(nullable = false)
    private @NonNull JobStatus publishStatus = JobStatus.PENDING;

    @Lob
    @Column(length = 1024 * 1024)
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

    @Column(name = "inferred_encoding")
    private String encoding;

    @Column(name = "format")
    private DataSourceMetadata.DataSourceType format;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dataset_sample_properties")
    private List<SampleProperty> sampleProperties = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dataset_options", joinColumns = { //
            @JoinColumn(name = "dataset_id", referencedColumnName = "id")//
    })
    @MapKeyColumn(name = "name")
    @Column(name = "value", columnDefinition = "TEXT")
    private Map<String, String> options;

    private PublishSettings publishing = new PublishSettings();

}
