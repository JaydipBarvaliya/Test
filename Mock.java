#!/bin/bash

# ==========================================================
# Script: mount-dgvlm.sh
# Purpose: Mount NAS using decrypted secret
# ==========================================================

set -euo pipefail

NAS_SERVER="//NSAPDVCS01.D2-TDBFG.COM/SRCHD_0027D"
MOUNT_POINT="/mnt/dgvlm-nas"
DOMAIN="D2-TDBFG"

APP_USER="springboot"
APP_GROUP="springboot"

ENCRYPTED_FILE="/opt/springboot/security/encrypted_json.enc"
PRIVATE_KEY="/etc/pki/TD_SPRINGBOOT/private-key.pem"

echo "Starting NAS mount process..."

# ----------------------------------------------------------
# 1️⃣ Ensure cifs-utils exists
# ----------------------------------------------------------

if ! command -v mount.cifs &>/dev/null; then
    echo "Installing cifs-utils..."
    sudo yum install -y cifs-utils
fi

# ----------------------------------------------------------
# 2️⃣ Ensure mount directory exists
# ----------------------------------------------------------

if [ ! -d "$MOUNT_POINT" ]; then
    echo "Creating mount directory..."
    sudo mkdir -p "$MOUNT_POINT"
    sudo chown "$APP_USER:$APP_GROUP" "$MOUNT_POINT"
    sudo chmod 775 "$MOUNT_POINT"
fi

# ----------------------------------------------------------
# 3️⃣ Skip if already mounted (safer check)
# ----------------------------------------------------------

if mountpoint -q "$MOUNT_POINT"; then
    echo "NAS already mounted."
    exit 0
fi

# ----------------------------------------------------------
# 4️⃣ Decrypt NAS password
# ----------------------------------------------------------

echo "Decrypting NAS password..."

NAS_PASSWORD=$(openssl smime -decrypt \
    -in "$ENCRYPTED_FILE" \
    -binary -inform DEM \
    -inkey "$PRIVATE_KEY" | \
    jq -r '."local"."secrets:naspassword"')

if [ -z "$NAS_PASSWORD" ] || [ "$NAS_PASSWORD" == "null" ]; then
    echo "ERROR: Failed to retrieve NAS password."
    exit 1
fi

# ----------------------------------------------------------
# 5️⃣ Mount NAS
# ----------------------------------------------------------

echo "Mounting NAS..."

sudo mount -t cifs "$NAS_SERVER" "$MOUNT_POINT" \
  -o username=TDGVLM942NASB,password="$NAS_PASSWORD",domain="$DOMAIN",uid="$APP_USER",gid="$APP_GROUP",vers=3.0,file_mode=0775,dir_mode=0775,nounix,_netdev

echo "NAS mounted successfully."

# ----------------------------------------------------------
# 6️⃣ Clear secret from memory
# ----------------------------------------------------------

unset NAS_PASSWORD