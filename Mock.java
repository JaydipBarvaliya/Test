#!/bin/bash
set -euo pipefail

### ARG CHECK ###
if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <version>"
  echo "Example: $0 0.0.22"
  exit 1
fi

VERSION="$1"

### CONSTANTS ###
REPO_URL="https://repo.td.com/repository/eets-maven-releases"
GROUP_PATH="com/td/besig"
ARTIFACT="besig-ops-api"

SERVICE="springboot-besig-ops-api"
DEPLOY_DIR="/opt/springboot/applications/besig-ops-api"
FINAL_JAR="${ARTIFACT}.jar"

BACKUP_DIR="/tmp/besig-ops"
BACKUP_JAR="${BACKUP_DIR}/${FINAL_JAR}"

TMP_JAR="/tmp/${ARTIFACT}-${VERSION}.jar"

OWNER="springboot:springboot"

HEALTH_URL="http://localhost:8080/actuator/health"
HEALTH_RETRIES=10
HEALTH_SLEEP=5

### DERIVED ###
JAR_URL="${REPO_URL}/${GROUP_PATH}/${ARTIFACT}/${VERSION}/${ARTIFACT}-${VERSION}.jar"

log() {
  echo "[INFO] $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

fail() {
  echo "[ERROR] $1"
  exit 1
}

log "Starting deployment of ${ARTIFACT} version ${VERSION}"

### STOP SERVICE ###
log "Stopping service ${SERVICE}"
sudo service "${SERVICE}" stop

### BACKUP ###
log "Preparing backup directory"
sudo mkdir -p "${BACKUP_DIR}"

if [[ -f "${DEPLOY_DIR}/${FINAL_JAR}" ]]; then
  log "Backing up existing jar to ${BACKUP_JAR}"
  sudo cp "${DEPLOY_DIR}/${FINAL_JAR}" "${BACKUP_JAR}"
else
  log "No existing jar found, skipping backup"
fi

### DOWNLOAD ###
log "Downloading jar from Nexus"
curl -fL "${JAR_URL}" -o "${TMP_JAR}"

### DEPLOY ###
log "Deploying new jar"
sudo mv "${TMP_JAR}" "${DEPLOY_DIR}/${FINAL_JAR}"
sudo chmod 765 "${DEPLOY_DIR}/${FINAL_JAR}"
sudo chown "${OWNER}" "${DEPLOY_DIR}/${FINAL_JAR}"

### START SERVICE ###
log "Starting service ${SERVICE}"
sudo service "${SERVICE}" start

### HEALTH CHECK ###
log "Waiting for health check"
for ((i=1; i<=HEALTH_RETRIES; i++)); do
  if curl -fs "${HEALTH_URL}" | grep -q '"status":"UP"'; then
    log "Health check passed"
    log "Deployment successful"
    exit 0
  fi
  log "Health not UP yet (attempt ${i}/${HEALTH_RETRIES})"
  sleep "${HEALTH_SLEEP}"
done

fail "Health check failed after deployment"