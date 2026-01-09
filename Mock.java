#!/bin/bash
set -euo pipefail

########################################
# 🎨 COLORS
########################################
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
BOLD='\033[1m'
NC='\033[0m' # No Color

########################################
# 📌 CONSTANTS
########################################
REPO_URL="https://repo.td.com/repository/eets-maven-releases"
GROUP_PATH="com/td/besig"
BASE_DEPLOY_DIR="/opt/springboot/applications"
OWNER="springboot:springboot"

HEALTH_RETRIES=10
HEALTH_SLEEP=5
LOCALHOST="127.0.0.1"

########################################
# 📦 APP CONFIG
# format:
# app-name | systemd-service | deploy-mode | health-type | health-value
########################################
APPS=(
  "besig-ops-api|springboot-besig-ops-api|service|http|https://besig.dev.td.com:8443/esig-ops/actuator/health"
  "besig-report|springboot-besig-report|service|tcp|8082"
  "besig-sp|springboot-besig-sp|service|tcp|8096"
  "besig-diamond|springboot-besig-diamond|batch|none|none"
)

########################################
# 🧱 LOGGING
########################################
log() {
  echo -e "${CYAN}[INFO]$(date '+%Y-%m-%d %H:%M:%S')${NC} $1"
}

warn() {
  echo -e "${YELLOW}[WARN]${NC} $1"
}

error() {
  echo -e "${RED}[ERROR]${NC} $1"
}

success() {
  echo -e "${GREEN}[SUCCESS]${NC} $1"
}

fail() {
  error "$1"
  exit 1
}

########################################
# 🚀 MAIN LOOP
########################################
while true; do
  echo
  echo -e "${BOLD}${BLUE}Select application to deploy${NC}"
  echo -e "${BLUE}--------------------------------${NC}"

  for i in "${!APPS[@]}"; do
    APP_NAME="${APPS[$i]%%|*}"
    printf "${MAGENTA}%d)${NC} %s\n" "$((i+1))" "$APP_NAME"
  done
  echo -e "${MAGENTA}0)${NC} Exit"
  echo

  read -rp "$(echo -e "${BOLD}Enter choice:${NC} ")" choice
  [[ "$choice" == "0" ]] && exit 0

  if ! [[ "$choice" =~ ^[0-9]+$ ]] || (( choice < 1 || choice > ${#APPS[@]} )); then
    warn "Invalid choice"
    continue
  fi

  IFS='|' read -r APP_NAME SERVICE_NAME DEPLOY_MODE HEALTH_TYPE HEALTH_VALUE <<< "${APPS[$((choice-1))]}"

  log "Deploy mode : ${BOLD}${DEPLOY_MODE}${NC}"

  DEPLOY_DIR="${BASE_DEPLOY_DIR}/${APP_NAME}"
  FINAL_JAR="${APP_NAME}.jar"
  BACKUP_DIR="/tmp/${APP_NAME}"
  BACKUP_JAR="${BACKUP_DIR}/${FINAL_JAR}"

  ########################################
  # 🔍 VERSION CHECK LOOP
  ########################################
  while true; do
    echo
    read -rp "$(echo -e "${BOLD}Enter version to deploy for ${APP_NAME}:${NC} ")" VERSION
    [[ -n "$VERSION" ]] || continue

    TMP_JAR="/tmp/${APP_NAME}-${VERSION}.jar"
    JAR_URL="${REPO_URL}/${GROUP_PATH}/${APP_NAME}/${VERSION}/${APP_NAME}-${VERSION}.jar"

    log "Checking artifact availability in Nexus"
    if curl -s --head --fail "$JAR_URL" > /dev/null; then
      success "Artifact found for version ${VERSION}"
      break
    else
      warn "Artifact not found for version ${VERSION}"
      warn "$JAR_URL"
    fi
  done

  echo
  log "Application : ${BOLD}${APP_NAME}${NC}"
  log "Service     : ${BOLD}${SERVICE_NAME}${NC}"
  log "Version     : ${BOLD}${VERSION}${NC}"
  log "Health type : ${BOLD}${HEALTH_TYPE}${NC}"
  log "Health val  : ${BOLD}${HEALTH_VALUE}${NC}"
  echo

  read -rp "$(echo -e "${BOLD}Proceed with deployment? (y/n):${NC} ")" confirm
  [[ "$confirm" =~ ^[Yy]$ ]] || continue

  [[ -d "$DEPLOY_DIR" ]] || fail "Deploy dir not found: $DEPLOY_DIR"

  if [[ "$DEPLOY_MODE" == "service" ]]; then
    systemctl list-unit-files | grep -q "^${SERVICE_NAME}.service" \
      || fail "Service not found: ${SERVICE_NAME}"
  fi

  ########################################
  # 🛑 STOP
  ########################################
  log "Stopping service"
  if [[ "$DEPLOY_MODE" == "service" ]]; then
    sudo systemctl stop "$SERVICE_NAME"
  fi

  ########################################
  # 💾 BACKUP
  ########################################
  sudo mkdir -p "$BACKUP_DIR"
  if [[ -f "${DEPLOY_DIR}/${FINAL_JAR}" ]]; then
    sudo cp "${DEPLOY_DIR}/${FINAL_JAR}" "$BACKUP_JAR"
    log "Backup stored at ${BACKUP_JAR}"
  fi

  ########################################
  # ⬇️ DOWNLOAD
  ########################################
  log "Downloading jar"
  curl -fL "$JAR_URL" -o "$TMP_JAR"

  ########################################
  # 📦 DEPLOY
  ########################################
  sudo mv "$TMP_JAR" "${DEPLOY_DIR}/${FINAL_JAR}"
  sudo chmod 765 "${DEPLOY_DIR}/${FINAL_JAR}"
  sudo chown "$OWNER" "${DEPLOY_DIR}/${FINAL_JAR}"

  ########################################
  #ест
  # ▶️ START + HEALTH CHECK
  ########################################
  if [[ "$DEPLOY_MODE" == "service" ]]; then
    log "Starting service"
    sudo systemctl start "$SERVICE_NAME"

    log "Waiting for health check"
    for ((i=1; i<=HEALTH_RETRIES; i++)); do
      if [[ "$HEALTH_TYPE" == "http" ]]; then
        if curl -ks "$HEALTH_VALUE" | grep -q '"status":"UP"'; then
          success "HTTP health check passed"
          break
        fi
      else
        if ss -lnt "sport = :${HEALTH_VALUE}" | grep -q LISTEN; then
          success "Port ${HEALTH_VALUE} is listening"
          break
        fi
      fi

      warn "Health not ready yet (attempt ${i}/${HEALTH_RETRIES})"
      sleep "$HEALTH_SLEEP"
      [[ "$i" -eq "$HEALTH_RETRIES" ]] && fail "Health check failed"
    done
  else
    warn "Batch job detected. Skipping service start and health check."
  fi

  success "Deployment successful for ${APP_NAME} (${VERSION})"
done