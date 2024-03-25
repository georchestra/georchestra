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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

import lombok.Data;

@Data
@Embeddable
public class PublishSettings {
    @Column(name = "publish")
    private Boolean publish = false;

    @Column(name = "imported_name")
    private String importedName;

    @Column(name = "published_workspace")
    private String publishedWorkspace;

    @Column(name = "published_name")
    private String publishedName;

    @Column(name = "published_encoding")
    private String encoding;

    @Column(name = "published_srs")
    private String srs;

    @Column(name = "pubished_srs_reproject")
    private Boolean srsReproject;

    @Column(name = "md_record_id")
    private String metadataRecordId;

    @Column(name = "md_title", length = 512)
    private String title;

    @Column(name = "md_abstract")
    @Lob
    private String Abstract;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dataset_md_keywords")
    private List<String> keywords;

    @Column(name = "md_creation_date")
    private LocalDate datasetCreationDate;

    @Column(name = "md_scale")
    private Integer scale;

    @Column(name = "md_creation_process_desc")
    @Lob
    private String datasetCreationProcessDescription;

    @Embedded
    @AttributeOverrides({ //
            @AttributeOverride(name = "minx", column = @Column(name = "md_geog_minx")), //
            @AttributeOverride(name = "maxx", column = @Column(name = "md_geog_maxx")), //
            @AttributeOverride(name = "miny", column = @Column(name = "md_geog_miny")), //
            @AttributeOverride(name = "maxy", column = @Column(name = "md_geog_maxy")) })
    private Envelope geographicBoundingBox;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "publishsettings_options", joinColumns = { //
            @JoinColumn(name = "dataset_id", referencedColumnName = "id")//
    })
    @MapKeyColumn(name = "name")
    @Column(name = "value", columnDefinition = "TEXT")
    private Map<String, String> options;

    public boolean getPublish() {
        return this.publish == null ? false : this.publish.booleanValue();
    }
}
