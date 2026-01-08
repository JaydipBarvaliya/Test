#!/bin/bash
set -euo pipefail

### CONSTANTS ###
REPO_URL="https://repo.td.com/repository/eets-maven-releases"
GROUP_PATH="com/td/besig"
BASE_DEPLOY_DIR="/opt/springboot/applications"
OWNER="springboot:springboot"

HEALTH_RETRIES=10
HEALTH_SLEEP=5

### APP : SERVICE : HEALTH_URL MAPPING ###
# format: app-name:systemd-service-name:health-url
APPS=(
  "besig-ops-api:springboot-besig-ops-api:http://localhost:8080/actuator/health"
  "besig-diamond:springboot-besig-diamond:http://localhost:8081/actuator/health"
  "besig-sp:springboot-besig-sp:http://localhost:8082/actuator/health"
  "besig-report:springboot-besig-report:http://localhost:8083/actuator/health"
)

log() {
  echo "[INFO] $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

fail() {
  echo "[ERROR] $1"
  exit 1
}

while true; do
  echo
  echo "Select application to deploy"
  echo "----------------------------"

  for i in "${!APPS[@]}"; do
    APP_NAME="${APPS[$i]%%:*}"
    printf "%d) %s\n" "$((i+1))" "$APP_NAME"
  done

  echo "0) Exit"
  echo

  read -rp "Enter choice: " choice

  if [[ "$choice" == "0" ]]; then
    echo "Exiting."
    exit 0
  fi

  if ! [[ "$choice" =~ ^[0-9]+$ ]] || (( choice < 1 || choice > ${#APPS[@]} )); then
    echo "Invalid choice. Try again."
    continue
  fi

  SELECTED="${APPS[$((choice-1))]}"

  APP_NAME="$(cut -d: -f1 <<< "${SELECTED}")"
  SERVICE_NAME="$(cut -d: -f2 <<< "${SELECTED}")"
  HEALTH_URL="$(cut -d: -f3 <<< "${SELECTED}")"

  DEPLOY_DIR="${BASE_DEPLOY_DIR}/${APP_NAME}"
  FINAL_JAR="${APP_NAME}.jar"

  BACKUP_DIR="/tmp/${APP_NAME}"
  BACKUP_JAR="${BACKUP_DIR}/${FINAL_JAR}"

  ### VERSION LOOP WITH NEXUS CHECK ###
  while true; do
    echo
    read -rp "Enter version to deploy for ${APP_NAME}: " VERSION
    [[ -n "$VERSION" ]] || { echo "Version cannot be empty"; continue; }

    TMP_JAR="/tmp/${APP_NAME}-${VERSION}.jar"
    JAR_URL="${REPO_URL}/${GROUP_PATH}/${APP_NAME}/${VERSION}/${APP_NAME}-${VERSION}.jar"

    log "Checking artifact availability in Nexus"

    if curl -s --head --fail "${JAR_URL}" > /dev/null; then
      log "Artifact found for version ${VERSION}"
      break
    else
      echo
      echo "[WARN] Artifact not found for version: ${VERSION}"
      echo "[WARN] URL checked:"
      echo "       ${JAR_URL}"
      echo "Please enter a valid version."
    fi
  done

  echo
  log "Application : ${APP_NAME}"
  log "Service     : ${SERVICE_NAME}"
  log "Version     : ${VERSION}"
  log "Deploy dir  : ${DEPLOY_DIR}"
  log "Health URL  : ${HEALTH_URL}"
  echo

  read -rp "Proceed with deployment? (y/n): " confirm
  [[ "$confirm" =~ ^[Yy]$ ]] || continue

  ### SANITY CHECKS ###
  [[ -d "${DEPLOY_DIR}" ]] || fail "Deploy directory not found: ${DEPLOY_DIR}"

  if ! systemctl list-unit-files | grep -q "^${SERVICE_NAME}.service"; then
    fail "Systemd service not found: ${SERVICE_NAME}"
  fi

  ### STOP SERVICE ###
  log "Stopping service ${SERVICE_NAME}"
  sudo systemctl stop "${SERVICE_NAME}"

  ### BACKUP ###
  log "Preparing backup"
  sudo mkdir -p "${BACKUP_DIR}"

  if [[ -f "${DEPLOY_DIR}/${FINAL_JAR}" ]]; then
    sudo cp "${DEPLOY_DIR}/${FINAL_JAR}" "${BACKUP_JAR}"
    log "Backup created at ${BACKUP_JAR}"
  else
    log "No existing jar found, skipping backup"
  fi

  ### DOWNLOAD ###
  log "Downloading jar"
  curl -fL "${JAR_URL}" -o "${TMP_JAR}"

  ### DEPLOY ###
  log "Deploying new jar"
  sudo mv "${TMP_JAR}" "${DEPLOY_DIR}/${FINAL_JAR}"
  sudo chmod 765 "${DEPLOY_DIR}/${FINAL_JAR}"
  sudo chown "${OWNER}" "${DEPLOY_DIR}/${FINAL_JAR}"

  ### START SERVICE ###
  log "Starting service ${SERVICE_NAME}"
  sudo systemctl start "${SERVICE_NAME}"

  ### HEALTH CHECK ###
  log "Waiting for health check"

  for ((i=1; i<=HEALTH_RETRIES; i++)); do
    if curl -fs "${HEALTH_URL}" | grep -q '"status":"UP"'; then
      log "Health check passed"
      log "Deployment successful for ${APP_NAME} (${VERSION})"
      break
    fi

    log "Health not ready yet (attempt ${i}/${HEALTH_RETRIES})"
    sleep "${HEALTH_SLEEP}"

    if [[ "$i" -eq "${HEALTH_RETRIES}" ]]; then
      fail "Health check failed after deployment"
    fi
  done
done