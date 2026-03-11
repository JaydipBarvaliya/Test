Below is a clean, Confluence-ready documentation for the NAS mounting approach you implemented. I kept it structured so your team (DevOps / future devs) can understand it quickly. 🚀

⸻

NAS Drive Mounting Configuration – DGVLM

1. Overview

The DGVLM application mounts a NAS (Network Attached Storage) location on the VM using the Salt CIFS module.
The mount configuration is defined inside the CD.yml deployment configuration file, and the credentials are securely retrieved from Vault during deployment via the EDP pipeline.

The NAS drive is mounted on the VM under:

/mnt/dgvlm-nas-<LOB_ID>

Example:

/mnt/dgvlm-nas-tdiclaims

This allows the application to read/write files directly to the mounted NAS location.

⸻

2. CIFS Mount Configuration

The NAS mount configuration is defined under the salt_formula -> cifs section.

Example configuration:

cifs:
  - name: /mnt/dgvlm-nas-tdiclaims
    device: //NSAPDVCS03.D2-TDBFG.COM/ICDMS_007F3
    credential: TDGVLM942NASB
    username: springboot
    groupname: springboot
    dirmode: '0775'
    filemode: '0640'

Field Explanation

Field	Description
name	Local mount path on the VM
device	NAS share path
credential	Credential name used to fetch the password from Vault
username	Local Linux user that owns the mounted directory
groupname	Local Linux group for permissions
dirmode	Directory permission
filemode	File permission


⸻

3. Credential Handling (Important)

The NAS authentication uses Vault secrets managed through the EDP pipeline.

Example vault configuration:

operation_secrets:
  vault:
    - name: "TDGVLM942NASB"
      type: "static"

Key Behavior
	•	The credential value must exactly match the Vault secret name.
	•	During deployment, the EDP pipeline prompts the user to supply a value for the secret.
	•	The value supplied is treated as the password for the NAS account.

Example:

Credential Name : TDGVLM942NASB
Value entered   : <NAS account password>

Salt then automatically creates a credentials file on the VM:

/etc/credentials/<credential-name>

Example:

/etc/credentials/d2-tdbfg.tdgvlm942nasb

Example file content:

username=tdgvlm942nasb
password=<password provided in pipeline>
domain=d2-tdbfg

⚠️ Important:

There is no separate password field in the configuration.
The password is supplied as the value of the Vault secret during deployment.

⸻

4. Mount Path Convention

All NAS mounts follow this naming convention:

/mnt/dgvlm-nas-<LOB_ID>

Where:
	•	dgvlm-nas → constant prefix
	•	<LOB_ID> → Line Of Business identifier

Example:

/mnt/dgvlm-nas-tdiclaims

Here:

LOB_ID = tdiclaims


⸻

5. Adding Support for a New LOB

Whenever a new LOB requires a NAS location, the following changes are required.

Step 1 — Add CIFS Entry

Add a new entry under the cifs array.

Example:

cifs:
  - name: /mnt/dgvlm-nas-dna
    device: //NSAPDVCS03.D2-TDBFG.COM/DNA_SHARE
    credential: TDGVLM942NASB
    username: springboot
    groupname: springboot
    dirmode: '0775'
    filemode: '0640'


⸻

Step 2 — Mount Path

Follow the standard format:

/mnt/dgvlm-nas-<LOB_ID>

Examples:

/mnt/dgvlm-nas-dna
/mnt/dgvlm-nas-tdi
/mnt/dgvlm-nas-claims


⸻

Step 3 — Ensure Environment Mapping

Each environment must point to the correct NAS share.

Example:

Environment	NAS Server
DEV	NSAPDVCS03
SIT	SIT NAS Server
PAT	PAT NAS Server

Each environment block in CD.yml should contain the correct device path.

Example:

DEV
//NSAPDVCS03.D2-TDBFG.COM/ICDMS_007F3

SIT
//SIT-NAS-SERVER/ICDMS_XXXX


⸻

6. Deployment Flow

The mount process happens automatically during deployment.

Step-by-step flow
	1.	Developer pushes configuration changes.
	2.	EDP pipeline starts deployment.
	3.	Pipeline prompts for Vault secret values.
	4.	User provides the NAS password for the credential.
	5.	Salt retrieves the secret.
	6.	Salt generates credentials file:

/etc/credentials/<credential-name>

	7.	Salt executes the CIFS mount state.
	8.	NAS share gets mounted under:

/mnt/dgvlm-nas-<LOB_ID>


⸻

7. Verification Steps

After deployment, verify the mount using:

Check mount

mount | grep dgvlm

or

df -h | grep dgvlm

Check credential file

cat /etc/credentials/*

Check mount directory

ls -l /mnt/dgvlm-nas-tdiclaims


⸻

8. Troubleshooting

Authentication Failure

Error example:

STATUS_LOGON_FAILURE

Possible causes:
	•	Incorrect password supplied during deployment
	•	NAS account does not have share permission
	•	Incorrect domain

⸻

Mount Failure

Check logs:

dmesg | tail

or

journalctl -u salt-minion


⸻

9. Key Points to Remember
	•	Credential name must match Vault secret name.
	•	Password is provided during deployment through the EDP pipeline.
	•	No password is stored in configuration files.
	•	Each new LOB requires a new CIFS entry.
	•	Mount path always follows:

/mnt/dgvlm-nas-<LOB_ID>


⸻

✅ If you want, I can also give you two additional things that will make this documentation 10× better for TD teams:

1️⃣ Architecture diagram (Salt → Vault → NAS → VM mount)
2️⃣ Sequence diagram of the EDP deployment flow

Those help a lot during architecture reviews and onboarding.