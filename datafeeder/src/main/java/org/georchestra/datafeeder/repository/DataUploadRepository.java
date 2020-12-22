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
package org.georchestra.datafeeder.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.georchestra.datafeeder.model.DataUploadState;
import org.georchestra.datafeeder.model.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import lombok.NonNull;

public interface DataUploadRepository extends JpaRepository<DataUploadState, UUID> {

    List<DataUploadState> findAllByUsername(@NonNull String username);

    Optional<DataUploadState> findByJobId(@NonNull UUID jobId);

    @Modifying
    @Transactional
    @Query("update DataUploadState set status = :status where jobId = :jobId")
    int setJobStatus(@Param("jobId") UUID jobId, @Param("status") UploadStatus status);

    @Modifying
    @Transactional
    @Query("update DataUploadState set progress = :progress where jobId = :jobId")
    int setProgress(@Param("jobId") UUID jobId, @Param("progress") double progress);
}
