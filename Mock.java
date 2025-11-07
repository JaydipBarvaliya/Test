Got it ✅ — here’s a comprehensive and professional email draft combining all your findings, including both Update Transaction and Delete Transaction scenarios, Postman observations, controller behavior, and OneSpan integration notes.

⸻

Subject: Deprecation of text/plain Response Type for AESIG API (Update & Delete Transaction Endpoints)

⸻

Dear Consumer Team,

As part of our ongoing API standardization initiative, we are planning to remove the text/plain response type from the following AESIG API endpoints:
	•	PATCH /esignatureevents/{eventId} (Update Transaction)
	•	DELETE /esignatureevents/{eventId} (Delete Transaction)

⸻

🧩 Current Behavior

Update Transaction (PATCH):

@RequestMapping(
    method = RequestMethod.PATCH,
    value = "/esignatureevents/{eventId}",
    produces = {"text/plain", "application/json"},
    consumes = {"application/json"}
)

Delete Transaction (DELETE):

@RequestMapping(
    method = RequestMethod.DELETE,
    value = "/esignatureevents/{eventId}",
    produces = {"text/plain", "application/json"}
)

	•	When no Accept header is provided, Spring automatically defaults to the first media type listed in produces — i.e., text/plain.
	•	The AESIG API currently does not return any actual payload in these endpoints, only a 200 OK status with an empty response body.

⸻

🧠 Observations (from Testing)
	1.	OneSpan Integration
	•	OneSpan does not accept text/plain; it only supports application/json.
	•	Hence, there’s no scenario where OneSpan sends Accept: text/plain.
	•	Clients that already send Accept: application/json are not impacted.
	2.	Default Fallback Behavior
	•	If a client does not send an Accept header, AESIG automatically sends Accept: */* to OneSpan.
	•	Spring’s controller, however, will serve the response in the first listed produces type (text/plain), which OneSpan accepts as RAW response.
	3.	Potential Impact
	•	After we remove text/plain, any client not sending Accept will start receiving a JSON Content-Type (application/json) by default.
	•	If such clients parse the raw response or rely on a specific Content-Type, they may experience behavioral differences.

⸻

🧾 Example Comparison

Scenario	Current Behavior	After Removal of text/plain
Accept: text/plain	200 OK, Content-Type: text/plain	❌ 406 Not Acceptable
Accept: application/json	200 OK, Content-Type: application/json	✅ 200 OK, no change
No Accept header	200 OK, defaults to text/plain	✅ 200 OK, defaults to application/json


⸻

📸 Screenshots (for reference)
	•	Postman Results: show both PATCH and DELETE returning 200 OK with empty body.
	•	Code Snippets: show produces = {"text/plain", "application/json"} in both methods.
	•	Notes Summary: highlight that OneSpan accepts JSON and not plain text.

⸻

✅ Conclusion & Next Steps
	•	Clients that already use Accept: application/json are not impacted.
	•	We need to confirm with consumers who do not send any Accept header, as they will start receiving a JSON response (instead of raw/plain text).
	•	This change improves consistency across AESIG endpoints and aligns with OneSpan’s accepted media types.

⸻

🗓️ Tentative Timeline

We plan to deploy this change to DEV/UAT first for consumer validation, and later to PROD after confirming no consumer dependency on text/plain.
Please confirm your applications’ readiness by [insert target date, e.g., December 20, 2025].

⸻

If you have any questions or need assistance during testing, please reach out to the AESIG API team.

Best regards,
AESIG API Team
TD Bank

⸻

Would you like me to include the screenshots inline in the email (as embedded images) or just mention them as attachments with descriptive filenames (like DeleteTransaction_Postman.png, UpdateTransaction_Code.png)?