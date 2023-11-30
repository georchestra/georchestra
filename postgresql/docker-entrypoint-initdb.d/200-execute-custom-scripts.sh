#!/bin/bash

# Executing custom scripts located in CUSTOM_SCRIPTS_DIRECTORY if environment variable is set
if [[ -z "${CUSTOM_SCRIPTS_DIRECTORY}" ]]; then
  echo "[INFO] No CUSTOM_SCRIPTS_DIRECTORY env variable set"
else
  echo "[INFO] CUSTOM_SCRIPTS_DIRECTORY env variable set to ${CUSTOM_SCRIPTS_DIRECTORY}"
  # Regex is needed to execute all kind of files, including sh files. Warning : --regex not available in alpine images.
  run-parts -v "${CUSTOM_SCRIPTS_DIRECTORY}" --regex='.*'
  echo "[INFO] End executing custom scripts"
fi
