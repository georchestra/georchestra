/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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

package org.georchestra.analytics.dao;

import java.util.Date;
import java.util.List;

import org.georchestra.analytics.model.Stats;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatsRepo extends PagingAndSortingRepository<Stats, Long> {

	List getLayersStatisticsForUser(@Param("user") String user, @Param("startDate") String startDate, @Param("endDate") String endDate);

	List getLayersStatisticsForUserLimit(@Param("user") String user, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("limit") int limit);

	List getLayersStatisticsForGroup(@Param("group") String group, @Param("startDate") String startDate, @Param("endDate") String endDate);

	List getLayersStatisticsForGroupLimit(@Param("group") String group, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("limit") int limit);

	List getLayersStatistics(@Param("startDate") String startDate, @Param("endDate") String endDate);

	List getLayersStatisticsLimit(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("limit") int limit);

	List getLayersExtractionForUserLimit(@Param("user") String user,
										 @Param("startDate") String startDate, @Param("endDate") String endDate,
										 @Param("limit") int limit);

	List getLayersExtractionForUser(@Param("user") String user,
									@Param("startDate") String startDate, @Param("endDate") String endDate);


	List getLayersExtractionForGroupLimit(@Param("group") String group,
										  @Param("startDate") String startDate, @Param("endDate") String endDate,
										  @Param("limit") int limit);


	List getLayersExtractionForGroup(@Param("group") String group,
									 @Param("startDate") String startDate, @Param("endDate") String endDate);


	List getLayersExtractionLimit(@Param("startDate") String startDate, @Param("endDate") String endDate,
								  @Param("limit") int limit);

	List getLayersExtraction(@Param("startDate") String startDate, @Param("endDate") String endDate);

	List getFullLayersExtraction(@Param("startDate") String startDate, @Param("endDate") String endDate);
}