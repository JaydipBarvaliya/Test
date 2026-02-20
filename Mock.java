#!/bin/bash

# ==========================================================
# Script Name : mount-dgvlm-test.sh
# Purpose     : Install cifs-utils and mount DGVLM NAS
# WARNING     : Hardcoded password for testing ONLY
# ==========================================================

set -e

# -----------------------------
# CONFIGURATION
# -----------------------------

NAS_SERVER="//NSAPDVCS01.D2-TDBFG.COM/SRCHD_0027D"
MOUNT_POINT="/mnt/dgvlm-nas"

USERNAME="TDGVLM942NASB"
PASSWORD="PUT_REAL_PASSWORD_HERE"
DOMAIN="D2-TDBFG"

APP_USER="springboot"
APP_GROUP="springboot"

echo "Starting NAS mount test..."

# -----------------------------
# STEP 1: Install cifs-utils
# -----------------------------

echo "Installing cifs-utils..."
sudo yum install -y cifs-utils

# Verify installation
if ! command -v mount.cifs &> /dev/null; then
    echo "ERROR: cifs-utils installation failed."
    exit 1
fi

echo "cifs-utils installed successfully."

# -----------------------------
# STEP 2: Create mount directory
# -----------------------------

if [ ! -d "$MOUNT_POINT" ]; then
    echo "Creating mount directory at $MOUNT_POINT"
    sudo mkdir -p "$MOUNT_POINT"
    sudo chown $APP_USER:$APP_GROUP "$MOUNT_POINT"
    sudo chmod 775 "$MOUNT_POINT"
fi

# -----------------------------
# STEP 3: Unmount if already mounted
# -----------------------------

if mount | grep -q "$MOUNT_POINT"; then
    echo "Already mounted. Unmounting first..."
    sudo umount "$MOUNT_POINT"
fi

# -----------------------------
# STEP 4: Mount NAS
# -----------------------------

echo "Attempting mount..."

sudo mount -t cifs "$NAS_SERVER" "$MOUNT_POINT" \
  -o username="$USERNAME",password="$PASSWORD",domain="$DOMAIN",uid=$APP_USER,gid=$APP_GROUP,file_mode=0775,dir_mode=0775,vers=3.0,nounix

# -----------------------------
# STEP 5: Validate mount
# -----------------------------

if mount | grep -q "$MOUNT_POINT"; then
    echo "======================================"
    echo "SUCCESS: NAS mounted at $MOUNT_POINT"
    echo "======================================"
    ls -l "$MOUNT_POINT"
else
    echo "FAILED: NAS mount did not succeed."
    exit 1
fi

sudo mkdir -p /opt/scripts
sudo mv mount-dgvlm-test.sh /opt/scripts/
sudo chmod 750 /opt/scripts/mount-dgvlm-test.sh

