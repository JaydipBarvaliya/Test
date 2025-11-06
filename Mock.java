Perfect — that’s actually an important clarification.

From your screenshot, it looks like your API currently returns an HTTP 200 OK with an empty JSON body ({}) or empty text response, meaning no actual data payload is being sent — just the status code confirming success.

Here’s how you can slightly update the email to reflect this situation clearly and help consumers understand that the change will affect content type, not data semantics.

⸻

Revised Email Draft

Subject: Deprecation Notice: Removal of text/plain Response Type from eSignature Events API

⸻

Dear Consumer Team,

We would like to inform you that we are planning to remove the text/plain response type support from the PATCH /esignatureevents/{eventId} API endpoint.

Currently, this endpoint supports two response formats via the produces attribute:

produces = {"text/plain", "application/json"}

When no Accept header is provided, the API defaults to returning a text/plain response.

⸻

📢 Planned Change

We will soon remove text/plain from the supported response types:

produces = {"application/json"}


⸻

⚙️ What This Means for You

After this change:
	•	If your client explicitly sends Accept: text/plain, the API will respond with HTTP 406 – Not Acceptable.
	•	If no Accept header is provided, the response will default to application/json.
	•	The API currently does not return any payload, only an HTTP status code confirming success, so there is no change in actual data, only in the response Content-Type header.

For example:

Scenario	Old Behavior	New Behavior
Accept: text/plain	200 OK, Content-Type: text/plain, body: (empty)	❌ 406 Not Acceptable
No Accept header	200 OK, Content-Type: text/plain	✅ 200 OK, Content-Type: application/json
Accept: application/json	200 OK, Content-Type: application/json	✅ 200 OK, Content-Type: application/json


⸻

✅ Action Required

Please ensure that your client applications or integration layers:
	•	Send Accept: application/json in all requests.
	•	Do not depend on the text/plain Content-Type in any response validation or parsing logic.

⸻

🗓️ Tentative Timeline

We recommend completing the required changes by [insert target date, e.g., December 15, 2025].
This change will first be deployed to DEV and UAT, followed by PROD once all consumers confirm readiness.

If you have any questions or need support during testing, please contact the AESIG API team.

Thank you for your cooperation and support.

Best regards,
AESIG API Team
TD Bank

⸻

Would you like me to make this slightly shorter and more business-friendly (for internal posting or Teams announcement) or keep it as this formal technical notice version for email distribution?