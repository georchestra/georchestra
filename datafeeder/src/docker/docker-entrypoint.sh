#!/bin/bash

DIR=/docker-entrypoint.d

if [[ -d "$DIR" ]]
then
  # Regex is needed to execute all kind of files, including sh files. Warning : --regex not available in alpine images.
  /bin/run-parts --verbose "$DIR" --regex='.*'
fi

exec "$@"
