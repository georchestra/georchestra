#!/usr/bin/env bash

start_hour=2016-10-27
end_hour=2016-10-28

start_day=2016-10-01
end_day=2016-10-30

start_week=2016-09-20
end_week=2016-12-27

start_month=2015-10-20
end_month=2016-12-27

user=testuser
group=MOD_LDAPADMIN
org=psc


# Test conbinedRequest webservice :
# ---------------------------------

echo Test Stats.getRequestCountBetweenStartDateAndEndDateByHour
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_hour\",\"endDate\":\"$end_hour\"}" \
http://localhost:8280/analytics/ws/combinedRequests

echo Test Stats.getRequestCountBetweenStartDateAndEndDateByDay
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\"}" \
http://localhost:8280/analytics/ws/combinedRequests

echo Test Stats.getRequestCountBetweenStartDateAndEndDateByWeek
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_week\",\"endDate\":\"$end_week\"}" \
http://localhost:8280/analytics/ws/combinedRequests

echo Test Stats.getRequestCountBetweenStartDateAndEndDateByMonth
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_month\",\"endDate\":\"$end_month\"}" \
http://localhost:8280/analytics/ws/combinedRequests

echo Test Stats.getRequestCountForUserBetweenStartDateAndEndDateByHour
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_hour\",\"endDate\":\"$end_hour\",\"user\": \"$user\"}" \
http://localhost:8280/analytics/ws/combinedRequests

echo Test Stats.getRequestCountForUserBetweenStartDateAndEndDateByDay
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\",\"user\": \"$user\"}" \
http://localhost:8280/analytics/ws/combinedRequests

echo Test Stats.getRequestCountForUserBetweenStartDateAndEndDateByWeek
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_week\",\"endDate\":\"$end_week\",\"user\": \"$user\"}" \
http://localhost:8280/analytics/ws/combinedRequests

echo Test Stats.getRequestCountForUserBetweenStartDateAndEndDateByMonth
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_month\",\"endDate\":\"$end_month\",\"user\": \"$user\"}" \
http://localhost:8280/analytics/ws/combinedRequests

echo Test Stats.getRequestCountForGroupBetweenStartDateAndEndDateByHour
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_hour\",\"endDate\":\"$end_hour\",\"group\": \"$group\"}" \
http://localhost:8280/analytics/ws/combinedRequests

echo Test Stats.getRequestCountForGroupBetweenStartDateAndEndDateByDay
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\",\"group\": \"$group\"}" \
http://localhost:8280/analytics/ws/combinedRequests

echo Test Stats.getRequestCountForGroupBetweenStartDateAndEndDateByWeek
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_week\",\"endDate\":\"$end_week\",\"group\": \"$group\"}" \
http://localhost:8280/analytics/ws/combinedRequests

echo Test Stats.getRequestCountForGroupBetweenStartDateAndEndDateByMonth
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_month\",\"endDate\":\"$end_month\",\"group\": \"$group\"}" \
http://localhost:8280/analytics/ws/combinedRequests


# Test distinctUsers webservice :
# ----------------------------

echo Test Stats.getDistinctUsersByGroup
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\",\"group\":\"$group\"}" \
http://localhost:8280/analytics/ws/distinctUsers

echo Test Stats.getDistinctUsers
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\",\"user\":\"$user\"}" \
http://localhost:8280/analytics/ws/distinctUsers


# Test layersUsage webservice :
# -----------------------------

echo Test Stats.getLayersStatisticsForUser
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\",\"user\":\"$user\"}" \
http://localhost:8280/analytics/ws/layersUsage

echo Test Stats.getLayersStatisticsForUserLimit
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\",\"user\":\"$user\",\"limit\":\"10\"}" \
http://localhost:8280/analytics/ws/layersUsage

echo Test Stats.getLayersStatisticsForGroup
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\",\"group\":\"$group\"}" \
http://localhost:8280/analytics/ws/layersUsage

echo Test Stats.getLayersStatisticsForGroupLimit
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\",\"group\":\"$group\",\"limit\":\"10\"}" \
http://localhost:8280/analytics/ws/layersUsage

echo Test Stats.getLayersStatistics
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\"}" \
http://localhost:8280/analytics/ws/layersUsage

echo Test Stats.getLayersStatisticsLimit
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\",\"limit\":\"10\"}" \
http://localhost:8280/analytics/ws/layersUsage


# Test layersExtraction webservice :
# ----------------------------------

echo Test Stats.getLayersExtractionForUser
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\",\"user\":\"$user\"}" \
http://localhost:8280/analytics/ws/layersExtraction

echo Test Stats.getLayersExtractionForUserLimit
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\",\"user\":\"$user\",\"limit\":\"2\"}" \
http://localhost:8280/analytics/ws/layersExtraction

echo Test Stats.getLayersExtractionForGroup
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\",\"group\":\"$group\"}" \
http://localhost:8280/analytics/ws/layersExtraction

echo Test Stats.getLayersExtractionForGroupLimit
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\",\"group\":\"$group\",\"limit\":\"2\"}" \
http://localhost:8280/analytics/ws/layersExtraction

echo Test Stats.getLayersExtraction
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\"}" \
http://localhost:8280/analytics/ws/layersExtraction

echo Test Stats.getLayersExtractionLimit
curl -H "Content-Type: application/json" -X POST \
-d "{\"startDate\":\"$start_day\",\"endDate\":\"$end_day\",\"limit\":\"2\"}" \
http://localhost:8280/analytics/ws/layersExtraction
