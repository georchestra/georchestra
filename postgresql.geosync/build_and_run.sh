#!/bin/bash

docker rm -f geosync_database_1
docker build -t geosync_database .
docker run -d --name geosync_database_1 geosync_database

