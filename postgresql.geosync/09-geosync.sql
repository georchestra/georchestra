CREATE USER geosync WITH PASSWORD 'secret';

CREATE DATABASE geosync_data_open with owner geosync;
\connect geosync_data_open
CREATE EXTENSION postgis;

CREATE DATABASE geosync_data_rsct with owner geosync;
\connect geosync_data_rsct
CREATE EXTENSION postgis;


