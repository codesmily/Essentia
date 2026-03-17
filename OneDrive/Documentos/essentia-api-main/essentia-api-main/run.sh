#!/usr/bin/env bash

# Detectar branch atual
BRANCH=$(git rev-parse --abbrev-ref HEAD)

if [[ "$BRANCH" == "main" ]]; then
  export APP_ENV=prod
elif [[ "$BRANCH" == "staging" ]]; then
  export APP_ENV=staging
else
  export APP_ENV=dev
fi

echo "🚀 Iniciando ambiente ${APP_ENV} (branch: ${BRANCH})"
docker compose down
docker compose up --build
