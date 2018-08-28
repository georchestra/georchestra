#!/bin/bash

SCRIPT_FULL_PATH=`realpath "$0"` # /absolute/path/myscript.sh
SCRIPT_PATH=`dirname "$SCRIPT_FULL_PATH"`  # /absolute/path

# on a besoin ici uniquement des logs
paramfile="$HOME/.geosync.conf"
#local host login passwd workspace datastore pg_datastore db logs
source "$paramfile"

PATH_LOG="/var/log/$logs"

# date dans les logs
date >> $PATH_LOG/init.log
date >> $PATH_LOG/init_error.log
  
# appel de init.sh
bash "${SCRIPT_PATH}/init.sh" -v -c 1>>$PATH_LOG/init.log 2>>$PATH_LOG/init_error.log

