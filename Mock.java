Good. Now this is no longer technical — this is influence and clarity.

The escalation email must do two things clearly:
	1.	Explain what is driving urgency (business impact).
	2.	Explain what happens if not delivered.

It must be professional, factual, and non-emotional.

Below is a clean escalation email you can send to ITS-Cloud-DevEng@td.com.

⸻

:::writing{id=“48291” variant=“email” subject=“Escalation – DSOPPE-9171 | NAS Mounting Required for DGVLM Deployment on VMC2”}
Hi Team,

I am writing to request prioritization support for Jira DSOPPE-9171 regarding execution of a shell script during artifact deployment in EDP (VMC2).

Business Driver:
The DGVLM application requires the NAS drive to be mounted automatically before the Spring Boot service starts. Without this, the application cannot access required files on the NAS share. This is a prerequisite for completing our deployment and meeting upcoming delivery milestones tracked by multiple LOB stakeholders.

Impact if Not Addressed:
If EDP cannot support running the mount script as part of the deployment lifecycle (or provide an equivalent supported mechanism), we will be unable to proceed with production deployment. This will delay the critical project timeline and impact dependent deliverables across teams.

We are not requesting manual server access changes; rather, we are seeking a supported deployment-level solution (e.g., equivalent of jboss_eap_instance_app_script_files for Spring Boot applications, or another EDP-recommended approach).

Given the time sensitivity and cross-team dependency, we would appreciate guidance on the supported path forward and prioritization assistance.

Please let me know if additional technical details are required.

Thank you for your support,
Jaydip Barvaliya
DGVLM – POD CI & Orch 1
:::

⸻

Now some strategic advice:
	•	Do NOT say “we are blocked because Jenkins overwrites files.”
	•	Do NOT blame infra.
	•	Frame it as “need supported deployment-level solution.”

That keeps it professional and aligned.

If you want, I can also help you prepare a short talking-point version in case they call you.