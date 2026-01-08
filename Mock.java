#!/bin/bash
set -euo pipefail

### CONSTANTS ###
REPO_URL="https://repo.td.com/repository/eets-maven-releases"
GROUP_PATH="com/td/besig"
BASE_DEPLOY_DIR="/opt/springboot/applications"
OWNER="springboot:springboot"

### APP LIST ###
APPS=(
  "basic-ops-api"
  "basic-diamond-api"
  "basic-report-api"
  "basic-sp-api"
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
  echo "Select application to deploy:"
  echo "-----------------------------"

  for i in "${!APPS[@]}"; do
    printf "%d) %s\n" "$((i+1))" "${APPS[$i]}"
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

  APP_NAME="${APPS[$((choice-1))]}"
  SERVICE_NAME="springboot-${APP_NAME}"
  DEPLOY_DIR="${BASE_DEPLOY_DIR}/${APP_NAME}"

  echo
  read -rp "Enter version to deploy for ${APP_NAME}: " VERSION

  [[ -n "$VERSION" ]] || {
    echo "Version cannot be empty"
    continue
  }

  FINAL_JAR="${APP_NAME}.jar"
  TMP_JAR="/tmp/${APP_NAME}-${VERSION}.jar"

  BACKUP_DIR="/tmp/${APP_NAME}"
  BACKUP_JAR="${BACKUP_DIR}/${FINAL_JAR}"

  JAR_URL="${REPO_URL}/${GROUP_PATH}/${APP_NAME}/${VERSION}/${APP_NAME}-${VERSION}.jar"

  echo
  log "Application : ${APP_NAME}"
  log "Version     : ${VERSION}"
  log "Service     : ${SERVICE_NAME}"
  log "Deploy dir  : ${DEPLOY_DIR}"
  log "Jar URL     : ${JAR_URL}"
  echo

  read -rp "Proceed with deployment? (y/n): " confirm
  [[ "$confirm" == "y" || "$confirm" == "Y" ]] || continue

  ### SANITY CHECK ###
  [[ -d "${DEPLOY_DIR}" ]] || fail "Deploy directory not found: ${DEPLOY_DIR}"

  ### STOP SERVICE ###
  log "Stopping service"
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
  log "Starting service"
  sudo systemctl start "${SERVICE_NAME}"

  log "Deployment completed for ${APP_NAME} ${VERSION}"
done