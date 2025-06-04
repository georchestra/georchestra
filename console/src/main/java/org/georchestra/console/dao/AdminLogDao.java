/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.console.dao;

import java.util.Collection;
import java.util.List;

import org.georchestra.console.model.AdminLogEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AdminLogDao extends PagingAndSortingRepository<AdminLogEntry, Long> {

    @Transactional
    List<AdminLogEntry> findByAdmin(String admin);

    @Transactional
    List<AdminLogEntry> findByTarget(String target);

    @Transactional
    List<AdminLogEntry> findByTarget(String target, Pageable range);

    List<AdminLogEntry> myFindByTargets(@Param("targets") Collection<String> targets, Pageable range);

}
