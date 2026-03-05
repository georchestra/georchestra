#!/bin/bash

DIR=/docker-entrypoint.d

# Executing custom scripts located in CUSTOM_SCRIPTS_DIRECTORY if environment variable is set
if [[ -z "${CUSTOM_SCRIPTS_DIRECTORY}" ]]; then
  echo "[INFO] No CUSTOM_SCRIPTS_DIRECTORY env variable set"
else
  echo "[INFO] CUSTOM_SCRIPTS_DIRECTORY env variable set to ${CUSTOM_SCRIPTS_DIRECTORY}"
  cp -v "${CUSTOM_SCRIPTS_DIRECTORY}"/* "$DIR"
  echo "[INFO] End copying custom scripts"
fi

if [[ -d "$DIR" ]]
then
  # Run all executable files in the directory.
  # Avoid --verbose and --regex as they are not available in BusyBox run-parts (e.g. Alpine images).
  /bin/run-parts "$DIR"
fi

exec "$@"
