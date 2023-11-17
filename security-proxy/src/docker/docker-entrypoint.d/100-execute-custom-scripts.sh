#!/bin/bash

# Executing custom scripts located in CUSTOM_SCRIPTS_DIRECTORY if environment variable is set
if [[ -z "${CUSTOM_SCRIPTS_DIRECTORY}" ]]; then
  echo "[INFO] No CUSTOM_SCRIPTS_DIRECTORY env variable set"
else
  echo "[INFO] CUSTOM_SCRIPTS_DIRECTORY env variable set to ${CUSTOM_SCRIPTS_DIRECTORY}"
  # Regex is needed in jetty9 images, but not alpine's ones.
  run-parts -v "${CUSTOM_SCRIPTS_DIRECTORY}" --regex='.*'
  echo "[INFO] End executing custom scripts"
fi
