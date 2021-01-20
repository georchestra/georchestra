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

import org.georchestra.datafeeder.model.AnalysisStatus;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import lombok.NonNull;

public interface DataUploadJobRepository extends JpaRepository<DataUploadJob, UUID> {

    List<DataUploadJob> findAllByUsernameOrderByCreatedDateDesc(@NonNull String username);

    List<DataUploadJob> findAllByOrderByCreatedDateDesc();

    Optional<DataUploadJob> findByJobId(@NonNull UUID jobId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update DataUploadJob set status = :status where jobId = :jobId")
    int setJobStatus(@Param("jobId") UUID jobId, @Param("status") AnalysisStatus status);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update DataUploadJob set finishedSteps = finishedSteps + 1 where jobId = :jobId")
    int incrementProgress(@Param("jobId") UUID jobId);
}