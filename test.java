Perfect 👍 here’s your Confluence-ready page for the “Update Transaction” endpoint — formatted to look exactly like your Apply Signature design doc when pasted directly into Confluence.
All tables and blocks are in Confluence-native syntax, so the formatting won’t break.

⸻

🟦 AESIG API – Response Media Type Review (Update Transaction Endpoint)

⸻

🔹 Endpoint Details

Property	Value
Endpoint	PATCH /esignatureevents/{eventId}
Consumes	application/json
Current Produces	text/plain, application/json
Proposed Produces	application/json


⸻

⚙️ Current Behavior Overview

The Update Transaction endpoint currently supports both text/plain and application/json.
However, it does not return any actual response body — only a 200 OK with Content-Length: 0.

If a client doesn’t send an Accept header, Spring defaults to the first value in the produces list (text/plain).
If Accept: application/json is sent, the response is returned with JSON headers (though still empty).

This dual-type setup causes inconsistent defaults across consumers and environments.

⸻

📊 Behavior Matrix – Before and After Change

Scenario	Current Behavior	Response Example	After Removing text/plain	Impact
Accept: application/json	✅ 200 OK	Content-Type: application/json	✅ 200 OK	None
Accept: text/plain	✅ 200 OK	Content-Type: text/plain	❌ 406 Not Acceptable	Must update Accept header
No Accept header	Defaults to text/plain	Content-Type: text/plain	Defaults to application/json	Verify parsing or response validators


⸻

💡 Root Cause / Reason for Change
	•	AESIG never returns a plain-text body.
	•	JSON is the standard and only supported format across AESIG APIs.
	•	Having text/plain in the produces list makes default behavior unpredictable.
	•	Removing it ensures consistent and REST-compliant responses.

⸻

📦 Example Response Snapshots

--- Before Change ---
Request: PATCH /esignatureevents/{eventId}
Accept: (not provided)
Response: 200 OK
Content-Type: text/plain
Content-Length: 0

Request: PATCH /esignatureevents/{eventId}
Accept: application/json
Response: 200 OK
Content-Type: application/json
Content-Length: 0

--- After Change ---
Request: PATCH /esignatureevents/{eventId}
Accept: (not provided)
Response: 200 OK
Content-Type: application/json
Content-Length: 0

Request: PATCH /esignatureevents/{eventId}
Accept: text/plain
Response: 406 Not Acceptable


⸻

🧠 Consumer Impact Summary

Consumer Type	Description	Action Required
✅ JSON Consumers	Already sending Accept: application/json	No change
⚠ No Accept Header Consumers	Default Content-Type will now be application/json	Verify parsing logic
❌ Plain-Text Consumers	Will receive HTTP 406	Update Accept header to application/json


⸻

✅ Recommendation
	•	Remove text/plain from produces.
	•	Standardize all AESIG response types to application/json.
	•	Inform consumers that plain text is deprecated.
	•	Validate in lower environments before rollout.

⸻

🚀 Rollout Plan

Phase	Action	Target Timeline
1️⃣	Review API Gateway logs for Accept: text/plain usage	Week 1
2️⃣	Deploy JSON-only version to DEV/UAT	Week 2
3️⃣	Notify consumers for validation	Week 3
4️⃣	Deploy to PROD after sign-off	Week 4


⸻

✅ End Result:
The Update Transaction endpoint will consistently return JSON responses, improving predictability, traceability, and alignment with other AESIG APIs.

⸻

Would you like me to generate the next Confluence-ready page (for Delete Transaction) in the exact same style and structure?