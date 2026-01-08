#!/bin/bash
set -euo pipefail

### CONSTANTS ###
REPO_URL="https://repo.td.com/repository/eets-maven-releases"
GROUP_PATH="com/td/besig"
BASE_DEPLOY_DIR="/opt/springboot/applications"
OWNER="springboot:springboot"

HEALTH_RETRIES=10
HEALTH_SLEEP=5
LOCALHOST="127.0.0.1"

### APP CONFIG ###
# format:
# app-name | systemd-service | health-type | health-value
# health-type = http | tcp
# health-value = URL (for http) OR port (for tcp)

APPS=(
  "besig-ops-api|springboot-besig-ops-api|http|https://localhost:8443/actuator/health"
  "besig-diamond|springboot-besig-diamond|tcp|8081"
  "besig-report|springboot-besig-report|tcp|8082"
  "besig-sp|springboot-besig-sp|tcp|8086"
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
    APP_NAME="${APPS[$i]%%|*}"
    printf "%d) %s\n" "$((i+1))" "$APP_NAME"
  done
  echo "0) Exit"
  echo

  read -rp "Enter choice: " choice

  [[ "$choice" == "0" ]] && exit 0

  if ! [[ "$choice" =~ ^[0-9]+$ ]] || (( choice < 1 || choice > ${#APPS[@]} )); then
    echo "Invalid choice"
    continue
  fi

  IFS='|' read -r APP_NAME SERVICE_NAME HEALTH_TYPE HEALTH_VALUE <<< "${APPS[$((choice-1))]}"

  DEPLOY_DIR="${BASE_DEPLOY_DIR}/${APP_NAME}"
  FINAL_JAR="${APP_NAME}.jar"

  BACKUP_DIR="/tmp/${APP_NAME}"
  BACKUP_JAR="${BACKUP_DIR}/${FINAL_JAR}"

  ### VERSION CHECK LOOP ###
  while true; do
    echo
    read -rp "Enter version to deploy for ${APP_NAME}: " VERSION
    [[ -n "$VERSION" ]] || continue

    TMP_JAR="/tmp/${APP_NAME}-${VERSION}.jar"
    JAR_URL="${REPO_URL}/${GROUP_PATH}/${APP_NAME}/${VERSION}/${APP_NAME}-${VERSION}.jar"

    log "Checking artifact availability in Nexus"

    if curl -s --head --fail "${JAR_URL}" > /dev/null; then
      log "Artifact found for version ${VERSION}"
      break
    else
      echo "[WARN] Artifact not found for version ${VERSION}"
      echo "[WARN] ${JAR_URL}"
    fi
  done

  echo
  log "Application : ${APP_NAME}"
  log "Service     : ${SERVICE_NAME}"
  log "Version     : ${VERSION}"
  log "Health type : ${HEALTH_TYPE}"
  log "Health val  : ${HEALTH_VALUE}"
  echo

  read -rp "Proceed with deployment? (y/n): " confirm
  [[ "$confirm" =~ ^[Yy]$ ]] || continue

  [[ -d "${DEPLOY_DIR}" ]] || fail "Deploy dir not found: ${DEPLOY_DIR}"

  systemctl list-unit-files | grep -q "^${SERVICE_NAME}.service" \
    || fail "Service not found: ${SERVICE_NAME}"

  ### STOP ###
  log "Stopping service"
  sudo systemctl stop "${SERVICE_NAME}"

  ### BACKUP ###
  sudo mkdir -p "${BACKUP_DIR}"
  if [[ -f "${DEPLOY_DIR}/${FINAL_JAR}" ]]; then
    sudo cp "${DEPLOY_DIR}/${FINAL_JAR}" "${BACKUP_JAR}"
    log "Backup stored at ${BACKUP_JAR}"
  fi

  ### DOWNLOAD ###
  log "Downloading jar"
  curl -fL "${JAR_URL}" -o "${TMP_JAR}"

  ### DEPLOY ###
  sudo mv "${TMP_JAR}" "${DEPLOY_DIR}/${FINAL_JAR}"
  sudo chmod 765 "${DEPLOY_DIR}/${FINAL_JAR}"
  sudo chown "${OWNER}" "${DEPLOY_DIR}/${FINAL_JAR}"

  ### START ###
  log "Starting service"
  sudo systemctl start "${SERVICE_NAME}"

  ### HEALTH CHECK ###
  log "Waiting for health check"

  for ((i=1; i<=HEALTH_RETRIES; i++)); do
    if [[ "${HEALTH_TYPE}" == "http" ]]; then
      if curl -ks "${HEALTH_VALUE}" | grep -q '"status":"UP"'; then
        log "HTTP health check passed"
        break
      fi
    else
      if nc -z "${LOCALHOST}" "${HEALTH_VALUE}" 2>/dev/null; then
        log "TCP port ${HEALTH_VALUE} is open"
        break
      fi
    fi

    log "Health not ready yet (attempt ${i}/${HEALTH_RETRIES})"
    sleep "${HEALTH_SLEEP}"

    [[ "$i" -eq "${HEALTH_RETRIES}" ]] && fail "Health check failed"
  done

  log "Deployment successful for ${APP_NAME} (${VERSION})"
done