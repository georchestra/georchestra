/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.NonNull;

@Data
@Entity
@Table(name = "upload")
@EntityListeners(AuditingEntityListener.class)
public class DataUploadJob {

    @Id
    @Column(unique = true, nullable = false)
    private UUID jobId;

    @Column(name = "created_by", nullable = false)
    private String username;

    @CreatedDate
    @Column(name = "created_date", nullable = false)
    private Date createdDate;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Column(nullable = false)
    @Basic(fetch = FetchType.EAGER)
    private JobStatus analyzeStatus = JobStatus.PENDING;

    @Column(nullable = false)
    @Basic(fetch = FetchType.EAGER)
    private JobStatus publishStatus = JobStatus.PENDING;

    @Lob
    @Column(length = 1024 * 1024)
    @Basic(fetch = FetchType.EAGER)
    private String error;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DatasetUploadState> datasets = new ArrayList<>();

    @Deprecated
    private int totalSteps;
    @Deprecated
    private int finishedSteps;

    /**
     * @return all uploaded datasets
     */
    public List<DatasetUploadState> getDatasets() {
        return this.datasets;
    }

    /**
     * @return all uploaded datasets that have been marked as
     *         {@link PublishSettings#isPublish() intended to be published} by
     *         inspecting {@code DatasetUploadState.getPublishing().isPublish()}
     */
    public List<DatasetUploadState> getPublishableDatasets() {
        return getDatasets().stream().filter(d -> d.getPublishing() != null && d.getPublishing().getPublish())
                .collect(Collectors.toList());
    }

    public Optional<DatasetUploadState> getDataset(@NonNull String nativeName) {
        return getDatasets().stream().filter(d -> nativeName.equals(d.getName())).findFirst();
    }

    public Optional<DatasetUploadState> firstDataset() {
        List<DatasetUploadState> dsets = getDatasets();
        return dsets == null || dsets.isEmpty() ? Optional.empty() : Optional.of(dsets.get(0));
    }
}
