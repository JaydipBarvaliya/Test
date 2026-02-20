sudo mkdir -p /opt/scripts
sudo mv mount-dgvlm-test.sh /opt/scripts/
sudo chmod 750 /opt/scripts/mount-dgvlm-test.sh

Thanks for the clarification.

That makes sense regarding the remount requirement after reboot or before app startup. I agree that relying purely on the script would require explicit execution unless we integrate it into startup.

For the credential handling, I’ve updated the approach to align with the existing secret retrieval mechanism. Instead of hardcoding the NAS password, I’m decrypting it from the encrypted_json.enc using the same openssl + jq pattern that we currently use for DB secrets. The password is stored only in memory and cleared after mount.

This keeps the implementation consistent with our current secret management pattern and avoids storing credentials in plain text.

For other environments, I’m planning to parameterize the NAS path and rely on environment-specific encrypted_json files so each environment uses its own secret values.

Let me know if you’d prefer we move this to an fstab-based mount instead of script-based startup integration.



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
