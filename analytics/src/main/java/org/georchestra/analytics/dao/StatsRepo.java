package org.georchestra.analytics.dao;

import java.util.Date;
import java.util.List;

import org.georchestra.analytics.model.Stats;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatsRepo extends PagingAndSortingRepository<Stats, Long> {

    public List getRequestCountForUserBetweenStartDateAndEndDate(@Param("user") String user,
    		@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    public List getRequestCountForGroupBetweenStartDateAndEndDate(@Param("group") String user,
    		@Param("startDate") Date startDate, @Param("endDate") Date endDate);

}