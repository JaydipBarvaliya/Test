Perfect 👍 Here’s your Confluence-ready page for the “Apply Signature” endpoint, fully aligned with the structure and clarity of your Update Transaction and Delete Transaction documents.
It also includes the OneSpan-specific logic you mentioned — where AESIG enforces Accept: application/json programmatically.

⸻

🟦 AESIG API – Response Media Type Review (Apply Signature Endpoint)

⸻

🔹 Endpoint Details

Property	Value
Endpoint	POST /esignatureevents/{eventId}/parties/{partyId}/signs
Consumes	application/json
Current Produces	binary/octet-stream, application/json
Proposed Produces	application/json


⸻

⚙️ Current Behavior Overview

The Apply Signature endpoint currently defines two produces types:
binary/octet-stream and application/json.
However, AESIG never returns any binary payload from this endpoint.
The response body is always empty (Content-Length: 0), with only a 200 OK status.

Here’s how it behaves today:
	1.	If the client sends Accept: binary/octet-stream, the controller resolves that type and returns Content-Type: binary/octet-stream, though no binary data is actually sent.
	2.	If the client sends Accept: application/json, the response is correctly returned with JSON headers.
	3.	In the AESIG delegate layer, the code explicitly forces Accept: application/json for downstream OneSpan API calls, since OneSpan only supports JSON:

httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));


	4.	Therefore, even if the consumer sends binary/octet-stream, AESIG still sends JSON to OneSpan.

This makes binary/octet-stream redundant at the AESIG layer and potentially misleading for consumers.

⸻

📊 Behavior Matrix – Before and After Change

Scenario	Current Behavior	Response Example	After Removing binary/octet-stream	Impact
Accept: application/json	✅ 200 OK	Content-Type: application/json	✅ 200 OK	None
Accept: binary/octet-stream	✅ 200 OK	Content-Type: binary/octet-stream	❌ 406 Not Acceptable	Must update to application/json
No Accept header	Defaults to binary/octet-stream (first in list)	Content-Type: binary/octet-stream	Defaults to application/json	Validate response handling
Accept: */*	✅ 200 OK	May resolve to binary/octet-stream	✅ 200 OK	Safer — will resolve to JSON post-change


⸻

💡 Root Cause / Reason for Change
	•	The AESIG layer does not produce binary content for this operation.
	•	Downstream OneSpan integration already forces application/json.
	•	Maintaining binary/octet-stream at this layer is unnecessary and misleading.
	•	Removing it simplifies client integration and ensures full alignment with OneSpan expectations.

⸻

📦 Example Response Snapshots

--- Before Change ---
Request: POST /esignatureevents/{eventId}/parties/{partyId}/signs
Accept: (not provided)
Response: 200 OK
Content-Type: binary/octet-stream
Content-Length: 0

Request: POST /esignatureevents/{eventId}/parties/{partyId}/signs
Accept: binary/octet-stream
Response: 200 OK
Content-Type: binary/octet-stream
Content-Length: 0

Request: POST /esignatureevents/{eventId}/parties/{partyId}/signs
Accept: application/json
Response: 200 OK
Content-Type: application/json
Content-Length: 0

--- After Change ---
Request: POST /esignatureevents/{eventId}/parties/{partyId}/signs
Accept: (not provided)
Response: 200 OK
Content-Type: application/json
Content-Length: 0

Request: POST /esignatureevents/{eventId}/parties/{partyId}/signs
Accept: binary/octet-stream
Response: 406 Not Acceptable


⸻

🧠 Consumer Impact Summary

Consumer Type	Description	Action Required
✅ JSON Consumers	Already sending Accept: application/json	No action required
⚠ No Accept Header Consumers	Will now default to application/json	Validate deserialization logic
❌ Binary Consumers	Using Accept: binary/octet-stream	Must update header to application/json


⸻

✅ Recommendation
	•	Remove binary/octet-stream from the produces list.
	•	Confirm that no clients in the AESIG gateway logs are explicitly using Accept: binary/octet-stream.
	•	Maintain consistency with OneSpan contract by enforcing JSON-only responses.
	•	Test with Postman and consumer mocks to verify successful 200 OK responses.

⸻

🚀 Rollout Plan

Phase	Action	Timeline
1️⃣	Review gateway logs for any clients using binary/octet-stream	Week 1
2️⃣	Deploy updated endpoint to DEV/UAT	Week 2
3️⃣	Communicate to all API consumers	Week 3
4️⃣	Remove binary/octet-stream from PROD after validation	Week 4


⸻

✅ End Result:
The Apply Signature endpoint will now only produce JSON, matching both AESIG’s internal response contract and OneSpan’s downstream requirements, eliminating ambiguity and ensuring content negotiation consistency.

⸻

Would you like me to proceed next with the Get Document endpoint (the final and most complex one with multiple produces types like binary/octet-stream, application/pdf, and application/json)?