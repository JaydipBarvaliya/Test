Perfect ✅ — this is exactly how to handle such changes professionally.
Here’s a ready-to-paste Confluence/Design-Doc draft for your lead and consumer teams.
It captures current behavior, all Accept header scenarios, impact after removal, and action items.

⸻

📘 AESIG API – Response Media Type Review (Apply Signature Endpoint)

🔹 Endpoint Details

Endpoint:
POST /esignatureevents/{eventId}/parties/{partyId}/signs

Consumes:
application/json

Current Produces:
binary/octet-stream, application/json

Proposed Change:
Remove binary/octet-stream from produces so that the endpoint only produces JSON.

⸻

🧩 Current Behavior Overview

The AESIG “Apply Signature” endpoint currently supports both binary/octet-stream and application/json response types.
However, the AESIG service does not return any binary data to clients — the body is empty (Content-Length: 0) with only 200 OK status.

Also, the downstream integration with OneSpan only accepts application/json, and AESIG already enforces this internally:

httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));

Therefore, binary/octet-stream support at the AESIG layer is redundant.

⸻

⚙️ Behavior Matrix – Before and After Change

Scenario	Client Request Header (Accept)	Current Behavior	Response Example	After Removing binary/octet-stream	Impact to Client
1️⃣ Client sends Accept: application/json	Matches produces list	✅ 200 OK, Content-Type: application/json	(Empty body)	✅ No change	None
2️⃣ Client sends Accept: binary/octet-stream	Matches produces list	✅ 200 OK, Content-Type: binary/octet-stream	(Empty body)	❌ 406 Not Acceptable	⚠️ Client must stop using this header
3️⃣ Client sends no Accept header	Treated as Accept: */*	Defaults to first produces value → binary/octet-stream	(Empty body)	Defaults to only produces type → application/json	✅ No functional impact (status 200 OK still)
4️⃣ Client sends unsupported Accept type (e.g., text/plain)	No match	❌ 406 Not Acceptable	—	❌ 406 Not Acceptable	None (already invalid)
5️⃣ AESIG → OneSpan internal request	Always forces Accept: application/json	✅ Accepted by OneSpan	JSON-only	✅ No change	None


⸻

🧠 Root Cause / Reason for Change
	•	AESIG never returns binary data from this endpoint.
	•	OneSpan only supports application/json.
	•	Keeping binary/octet-stream is unnecessary and may cause confusion or inconsistent content negotiation when clients omit the Accept header.

⸻

🧾 Example Response Snapshots

Postman Example – Current Behavior

Case	Request	Response
No Accept header	200 OK, Content-Type: binary/octet-stream	Content-Length: 0
Accept: application/json	200 OK, Content-Type: application/json	Content-Length: 0
Accept: binary/octet-stream	200 OK, Content-Type: binary/octet-stream	Content-Length: 0

After Change

Case	Request	Response
No Accept header	200 OK, Content-Type: application/json	Content-Length: 0
Accept: application/json	200 OK, Content-Type: application/json	Content-Length: 0
Accept: binary/octet-stream	❌ 406 Not Acceptable	—


⸻

🔍 Consumer Impact Summary

Impact Type	Description	Action Required
✅ Existing JSON clients	Already using Accept: application/json	No action required
⚠️ Non-JSON or no Accept header clients	Will now receive Content-Type: application/json	Verify client code doesn’t rely on raw/binary response parsing
❌ Clients using Accept: binary/octet-stream	Will receive HTTP 406	Must update header to Accept: application/json


⸻

✅ Recommendation
	•	Proceed with removing "binary/octet-stream" from the produces list.
	•	Monitor gateway logs to confirm no existing consumer is sending Accept: binary/octet-stream.
	•	Communicate to consumers:
	•	Use Accept: application/json for all AESIG API calls.
	•	Validate that they can handle JSON content type (even with empty bodies).

⸻

🗓️ Rollout Plan

Phase	Action	Timeline
1	Validate existing consumer Accept usage in API Gateway logs	Week 1
2	Update endpoint and deploy to DEV/UAT	Week 2
3	Notify consumers to validate	Week 3
4	Remove binary/octet-stream from PROD after sign-off	Week 4


⸻

📨 Sample Consumer Notification Email

Subject: AESIG API Update – Removal of binary/octet-stream Response Type

Dear Consumer Team,

We are planning to remove the binary/octet-stream response type from the Apply Signature API endpoint (POST /esignatureevents/{eventId}/parties/{partyId}/signs).

Going forward, only application/json responses will be supported. Clients currently sending Accept: application/json or no Accept header will not be impacted.

Clients sending Accept: binary/octet-stream will start receiving HTTP 406 – Not Acceptable.

Please ensure your integrations are updated to send Accept: application/json.

If you have questions or need assistance validating this change, contact the AESIG API team.

AESIG API Team – TD Bank

⸻

Would you like me to generate this in a Confluence markdown/table-compatible format (so it pastes cleanly with proper table borders and headers), or keep it as this clean markdown layout?