sudo mkdir -p /opt/scripts
sudo mv mount-dgvlm-test.sh /opt/scripts/
sudo chmod 750 /opt/scripts/mount-dgvlm-test.sh





#!/bin/bash

# ==========================================================
# Script: mount-dgvlm.sh
# Purpose: Mount NAS using decrypted secret
# ==========================================================

set -e

# -----------------------------
# CONFIGURATION
# -----------------------------

NAS_SERVER="//NSAPDVCS01.D2-TDBFG.COM/SRCHD_0027D"
MOUNT_POINT="/mnt/dgvlm-nas"
DOMAIN="D2-TDBFG"

APP_USER="springboot"
APP_GROUP="springboot"

ENCRYPTED_FILE="/opt/springboot/security/encrypted_json.enc"
PRIVATE_KEY="/etc/pki/TD_SPRINGBOOT/private-key.pem"

echo "Starting NAS mount process..."

# -----------------------------
# STEP 1: Install cifs-utils
# -----------------------------

if ! command -v mount.cifs &> /dev/null; then
    sudo yum install -y cifs-utils
fi

# -----------------------------
# STEP 2: Decrypt NAS password
# -----------------------------

echo "Decrypting NAS password..."

NAS_PASSWORD=$(openssl smime -decrypt \
    -in "$ENCRYPTED_FILE" \
    -binary -inform DEM \
    -inkey "$PRIVATE_KEY" | \
    jq -r '["local"]["secrets:naspassword"]')

if [ -z "$NAS_PASSWORD" ]; then
    echo "ERROR: Failed to retrieve NAS password."
    exit 1
fi

# -----------------------------
# STEP 3: Create mount directory
# -----------------------------

if [ ! -d "$MOUNT_POINT" ]; then
    sudo mkdir -p "$MOUNT_POINT"
    sudo chown $APP_USER:$APP_GROUP "$MOUNT_POINT"
    sudo chmod 775 "$MOUNT_POINT"
fi

# -----------------------------
# STEP 4: Mount
# -----------------------------

if mount | grep -q "$MOUNT_POINT"; then
    echo "Already mounted."
    exit 0
fi

echo "Mounting NAS..."

sudo mount -t cifs "$NAS_SERVER" "$MOUNT_POINT" \
    -o username=TDGVLM942NASB,password="$NAS_PASSWORD",domain="$DOMAIN",uid=$APP_USER,gid=$APP_GROUP,vers=3.0,file_mode=0775,dir_mode=0775,nounix

echo "NAS mounted successfully."

# -----------------------------
# STEP 5: Clear sensitive variable
# -----------------------------

unset NAS_PASSWORD
