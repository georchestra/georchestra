#!/bin/bash

rm -fr build/docker/*

img=`docker ps -q` && docker stop $img && docker rm $img && ./gradlew startDocker && docker logs -f `docker ps -q`

