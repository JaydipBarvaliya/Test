Perfect ✅ — let’s make this email both clear and professional, aligned with how TD’s internal API teams usually communicate such updates to external or partner consumers.

Below is a ready-to-send draft email you can paste into Outlook or Confluence announcement templates.
It keeps a balanced tone: informative, low-risk, and includes a technical example + action guidance.

⸻

Subject: AESIG API Update – Standardization of Response Media Types (Action Required)

⸻

Dear Consumer Team,

We’re reaching out to inform you about an upcoming change in the AESIG API response configuration as part of our ongoing standardization and alignment with TD’s API governance practices.

What’s Changing

Starting with the next release, we are removing support for text/plain and binary/octet-stream response types across AESIG APIs.
Only descriptive and REST-compliant response types (application/json and, where applicable, application/pdf) will be supported going forward.

This update applies to the following endpoints:

Endpoint	Current Produces	New Produces
PATCH /esignatureevents/{eventId}	text/plain, application/json	application/json
DELETE /esignatureevents/{eventId}	text/plain, application/json	application/json
POST /esignatureevents/{eventId}/parties/{partyId}/signs	binary/octet-stream, application/json	application/json
GET /esignatureevents/{eventId}/documentpackage/{documentId}	binary/octet-stream, application/pdf, application/json	application/pdf, application/json


⸻

Impact to Consumers
	•	✅ If you already send Accept: application/json (or application/pdf for document download), no action is required.
	•	⚠️ If your integration currently uses Accept: text/plain or Accept: binary/octet-stream, you may start receiving HTTP 406 – Not Acceptable responses.
	•	⚙️ If no Accept header is specified, responses will now default to application/json (or application/pdf for document retrieval).

⸻

Example Behavior Change

Scenario	Current Behavior	After Change
Accept: application/json	200 OK – JSON	200 OK – JSON ✅
Accept: text/plain	200 OK – Empty body	406 Not Acceptable ❌
Accept: binary/octet-stream	200 OK – Empty body	406 Not Acceptable ❌
No Accept header	200 OK – May default to text/plain	200 OK – Defaults to JSON ✅


⸻

Action Required
	•	Review your integration configurations for these endpoints.
	•	Ensure that Accept headers are updated to:
	•	application/json for all non-document API calls.
	•	application/pdf for the Get Document endpoint.
	•	Test your requests in lower environments (DEV/UAT) once the change is available.
	•	Validate response parsing logic for empty or JSON payloads.

⸻

Rollout Timeline

Phase	Action	Timeline
Week 1	Review API Gateway logs and identify impacted consumers	✔️ Completed / In Progress
Week 2	Deploy updated endpoints to DEV/UAT	Scheduled
Week 3	Consumer validation and testing window	Open
Week 4	Production rollout after sign-off	Planned


⸻

Need Help?

If you have questions or need assistance validating this change, please contact the AESIG API team at
📧 aesig-api-support@td.com￼

⸻

Thank you for your continued partnership as we enhance API consistency and reliability across the AESIG platform.

Kind regards,
AESIG API Team
TD Bank Group

⸻

Would you like me to also create a short internal version (for TD internal consumers on the API gateway, e.g., “FYI-only, no external mailing”)?
That version usually omits the rollout plan and contact info but keeps the table + examples compact.