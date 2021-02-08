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

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;

import lombok.Data;

@Data
@Embeddable
public class PublishSettings {
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

    @Column(name = "md_title")
    private String title;

    @Column(name = "md_abstract")
    private String Abstract;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dataset_md_keywords")
    private List<String> keywords;

    @Column(name = "md_creation_date")
    private LocalDate datasetCreationDate;

    @Column(name = "md_scale")
    private Double scale;

    @Column(name = "md_creation_process_desc")
    private String datasetCreationProcessDescription;
}
