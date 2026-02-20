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

echo "========== Starting NAS Mount Process =========="

# ----------------------------------------------------------
# 1️⃣ Install cifs-utils if missing
# ----------------------------------------------------------

if ! command -v mount.cifs &>/dev/null; then
    echo "cifs-utils not found. Installing..."
    if ! sudo yum install -y cifs-utils; then
        echo "ERROR: Failed to install cifs-utils."
        exit 1
    fi
fi

# Validate installation
if ! command -v mount.cifs &>/dev/null; then
    echo "ERROR: mount.cifs still not available after installation."
    exit 1
fi

echo "cifs-utils verified."

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
# 3️⃣ Check if already mounted
# ----------------------------------------------------------

if mountpoint -q "$MOUNT_POINT"; then
    echo "NAS already mounted at $MOUNT_POINT"
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

echo "NAS password retrieved successfully."

# ----------------------------------------------------------
# 5️⃣ Mount NAS
# ----------------------------------------------------------

echo "Mounting NAS..."

if ! sudo mount -t cifs "$NAS_SERVER" "$MOUNT_POINT" \
  -o username=TDGVLM942NASB,password="$NAS_PASSWORD",domain="$DOMAIN",uid="$APP_USER",gid="$APP_GROUP",vers=3.0,file_mode=0775,dir_mode=0775,nounix,_netdev; then
    echo "ERROR: Mount command failed."
    unset NAS_PASSWORD
    exit 1
fi

# ----------------------------------------------------------
# 6️⃣ Validate mount success
# ----------------------------------------------------------

if mountpoint -q "$MOUNT_POINT"; then
    echo "SUCCESS: NAS mounted at $MOUNT_POINT"
else
    echo "ERROR: NAS mount verification failed."
    unset NAS_PASSWORD
    exit 1
fi

# ----------------------------------------------------------
# 7️⃣ Clear sensitive variable
# ----------------------------------------------------------

unset NAS_PASSWORD

echo "========== NAS Mount Completed Successfully =========="