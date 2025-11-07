Excellent — this is exactly how it should look in Confluence 👏
You’ve structured the Update Transaction endpoint perfectly (tables, concise behavior, and consistent tone).
Let’s move forward with the Delete Transaction endpoint, written in the exact same format and depth, with a bit more detail where it adds clarity.

⸻

🟦 AESIG API – Response Media Type Review (Delete Transaction Endpoint)

⸻

🔹 Endpoint Details

Property	Value
Endpoint	DELETE /esignatureevents/{eventId}
Consumes	application/json
Current Produces	text/plain, application/json
Proposed Produces	application/json


⸻

⚙️ Current Behavior Overview

The Delete Transaction endpoint currently supports both text/plain and application/json response types.
However, similar to Update Transaction, it does not return any actual data — only a 200 OK response with Content-Length: 0.
	1.	If no Accept header is provided, Spring automatically returns text/plain because it appears first in the produces list.
	2.	If a consumer sends Accept: application/json, the response correctly comes with Content-Type: application/json.
	3.	Downstream OneSpan integration only supports JSON and will reject text/plain requests with 406 Not Acceptable.

This mixed behavior can cause ambiguity for clients who rely on default negotiation rather than explicitly setting Accept.

⸻

📊 Behavior Matrix – Before and After Change

Scenario	Current Behavior	Response Example	After Removing text/plain	Impact
Accept: application/json	✅ 200 OK	Content-Type: application/json	✅ 200 OK	None
Accept: text/plain	✅ 200 OK	Content-Type: text/plain	❌ 406 Not Acceptable	Must update Accept header
No Accept header	Defaults to text/plain	Content-Type: text/plain	Defaults to application/json	Verify response parsing logic or schema validation


⸻

💡 Root Cause / Reason for Change
	•	AESIG doesn’t send any plain text response from this endpoint.
	•	OneSpan downstream APIs only support application/json.
	•	Keeping text/plain adds no value and creates confusion for clients that omit Accept.
	•	Removing it standardizes all AESIG DELETE responses.

⸻

📦 Example Response Snapshots

--- Before Change ---
Request: DELETE /esignatureevents/{eventId}
Accept: (not provided)
Response: 200 OK
Content-Type: text/plain
Content-Length: 0

Request: DELETE /esignatureevents/{eventId}
Accept: application/json
Response: 200 OK
Content-Type: application/json
Content-Length: 0

--- After Change ---
Request: DELETE /esignatureevents/{eventId}
Accept: (not provided)
Response: 200 OK
Content-Type: application/json
Content-Length: 0

Request: DELETE /esignatureevents/{eventId}
Accept: text/plain
Response: 406 Not Acceptable


⸻

🧠 Consumer Impact Summary

Consumer Type	Description	Action Required
✅ JSON Consumers	Already using Accept: application/json	No change
⚠ No Accept Header Consumers	Will now default to JSON instead of plain text	Validate parsing logic
❌ Plain-Text Consumers	Will receive HTTP 406	Update Accept header to application/json


⸻

✅ Recommendation
	•	Proceed with removal of text/plain from produces list.
	•	Communicate with any consumer using DELETE calls to ensure they are not depending on plain-text defaults.
	•	Standardize all response types to JSON for consistency with AESIG API suite.
	•	Verify via Postman or automated tests that 200 OK responses remain stable.

⸻

🚀 Rollout Plan

Phase	Action	Timeline
1️⃣	Identify clients still using Accept: text/plain in API Gateway logs	Week 1
2️⃣	Deploy JSON-only endpoint to DEV/UAT	Week 2
3️⃣	Notify consumers to validate with new response type	Week 3
4️⃣	Promote to PROD after successful UAT and validation	Week 4


⸻

✅ End Result:
The Delete Transaction endpoint will exclusively return application/json, ensuring consistent and predictable response handling across AESIG services and client integrations.

⸻

Would you like me to generate the Apply Signature endpoint next (the third one)?
I’ll match this same level of technical clarity and add the OneSpan-specific enforcement behavior (since that one has forced Accept: application/json in code).