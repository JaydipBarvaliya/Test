Perfect. I’ll rewrite this properly structured, clean, and Confluence-ready so you can paste it directly into Jira/Confluence without editing.

Below is a single complete documentation file.

⸻

📘 DGVLM API – Specification & Certification Publication Process

🎯 Objective

This document describes the complete process to:
	•	Publish API scope to Marketplace
	•	Validate and release OpenAPI specifications
	•	Run certification workflows via GitHub Actions
	•	Ensure artifacts are correctly promoted to target repositories

This process must be followed exactly to avoid spec version inconsistencies or certification failures.

⸻

1️⃣ Repository Details

Primary API Config Repository

https://github.com/TD-Enterprise/dgvlm-api

Repository Type: Internal
Generated From: TD-Enterprise/edp-template-repo

⸻

2️⃣ Branch Strategy

You must:
	1.	Checkout main
	2.	Create a new branch:

dgvlm-api

OR use your feature-specific branch if applicable (e.g., aesig-api)
	3.	Replace your OpenAPI spec file inside:

certifications/

Example:

api.esignatureevents.esignlive.json
manifest_auto.json

⚠ Ensure your spec file is correct before triggering workflows.

⸻

3️⃣ GitHub Workflows Execution Order

All workflows must be executed in the exact sequence below.

Navigate to:

.github/workflows


⸻

✅ Step 1: Spec Workflow – Pre-Release

Workflow:

specification-workflow-caller.yml

Purpose:
	•	Performs validation
	•	Checks OpenAPI schema correctness
	•	Detects formatting or structure errors

🚨 Do not proceed if this step fails.

⸻

✅ Step 2: Spec Workflow – Release (feature/development branch)

Trigger release workflow on:
	•	feature branch
	•	development branch

Purpose:
	•	Publishes spec with _dev tag
	•	Pushes new spec version to:

https://github.com/TD-Universe/OAS-DGVLA-*****

You should see:

new spec version (with _dev tag)


⸻

✅ Step 3: Certification Workflow – Pre-Release

Workflow:

certification-workflow-caller.yml

Purpose:
	•	Validates certification artifacts
	•	Ensures manifest correctness
	•	Performs compliance checks

Again — do not move forward if this fails.

⸻

✅ Step 4: Certification Workflow – Release

Trigger release version of certification workflow.

Purpose:
	•	Uploads certification artifact
	•	Publishes to certification repository
	•	Prepares for automation pickup

⸻

4️⃣ Final Spec Promotion to Master

After validation on feature/dev:

Run:

Spec workflow – Release – master

Purpose:
	•	Publishes final spec version
	•	Removes _dev tag
	•	Creates official version in:

https://github.com/TD-Universe/OAS-DGVLA-*****

Expected Result:

New spec version (without _dev tag)


⸻

5️⃣ Verification Checklist

After all workflows complete:

✔ Confirm new version exists in:

TD-Universe/OAS-DGVLA-*****

✔ Confirm certification artifact uploaded

✔ Confirm no workflow failures

✔ Confirm branch is merged if required

⸻

6️⃣ Automation & Marketplace Availability

If all steps are completed successfully:
	•	Changes are picked up by automation next day
	•	Scope becomes available in Marketplace:

https://mrkp.prod.td.com/apistore/discover/

⏳ Timeline:

If completed today → Marketplace availability expected next business day (typically Monday if done Friday).

⸻

7️⃣ Common Mistakes to Avoid

❌ Running certification before spec release
❌ Skipping pre-release validation
❌ Publishing directly to master without feature validation
❌ Not checking _dev vs non-dev version
❌ Incorrect manifest file

⸻

8️⃣ Repository Structure Reference

Example branch:

aesig-api

Contains:

.github/workflows/
certifications/
CODEOWNERS
EDP.yml
README

Example certification files:

api.esignatureevents.esignlive.json
manifest_auto.json


⸻

9️⃣ High-Level Flow Diagram

Update Spec →
Spec Pre-Release →
Spec Release (Dev) →
Cert Pre-Release →
Cert Release →
Spec Release (Master) →
Automation Pickup →
Marketplace Availability


⸻

🔟 Summary

This process ensures:
	•	Spec validation
	•	Version control integrity
	•	Certification compliance
	•	Automated publishing
	•	Marketplace availability

Follow the workflow sequence strictly.

⸻

If you want, I can also:
	•	Format this into Confluence Wiki Markup
	•	Generate a visual architecture diagram
	•	Add a troubleshooting section
	•	Add screenshots mapping to each step
	•	Convert into a PDF-ready formal document

Just tell me 👍