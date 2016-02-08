package org.georchestra.analytics.dao;

import java.util.Date;
import java.util.List;

import org.georchestra.analytics.model.Stats;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatsRepo extends PagingAndSortingRepository<Stats, Long> {
	public List getRequestCountBetweenStartDateAndEndDateByHour(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
	public List getRequestCountBetweenStartDateAndEndDateByDay(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
	public List getRequestCountBetweenStartDateAndEndDateByWeek(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
	public List getRequestCountBetweenStartDateAndEndDateByMonth(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
	
	public List getRequestCountForUserBetweenStartDateAndEndDateByHour(@Param("user") String user,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	public List getRequestCountForUserBetweenStartDateAndEndDateByDay(@Param("user") String user,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	public List getRequestCountForUserBetweenStartDateAndEndDateByWeek(@Param("user") String user,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	public List getRequestCountForUserBetweenStartDateAndEndDateByMonth(@Param("user") String user,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	public List getRequestCountForGroupBetweenStartDateAndEndDateByHour(@Param("group") String group,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	public List getRequestCountForGroupBetweenStartDateAndEndDateByDay(@Param("group") String group,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	public List getRequestCountForGroupBetweenStartDateAndEndDateByWeek(@Param("group") String group,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	public List getRequestCountForGroupBetweenStartDateAndEndDateByMonth(@Param("group") String group,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	public List<String> getDistinctUsers(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
	
	public List<String> getDistinctUsersByGroup(@Param("group") String group,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);


	public List getLayersStatisticsForUser(@Param("user") String user,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	public List getLayersStatisticsForUserLimit(@Param("user") String user,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("limit") int limit);

	public List getLayersStatisticsForGroup(@Param("group") String group,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	public List getLayersStatisticsForGroupLimit(@Param("group") String group,
			@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("limit") int limit);

	public List getLayersStatistics(
			@Param("startDate") Date startDate, @Param("endDate") Date endDate);

	public List getLayersStatisticsLimit(
			@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("limit") int limit);

}