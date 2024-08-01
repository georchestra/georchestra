#!/bin/bash

for file in `ls /docker-entrypoint-initdb.d/*-data.sql`; do
  if [ "$IGNORE_DATA" = "yes" ]; then
   echo -n "" > $file
  fi
done
