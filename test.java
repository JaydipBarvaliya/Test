Excellent — this is the right endpoint to close the full chain.
Below is your Confluence-ready design page for the “Get Document” endpoint, formatted exactly like your previous three pages but with extra depth to capture the complexity of its behavior (three produces types and binary vs. PDF vs. JSON content differences).

⸻

🟦 AESIG API – Response Media Type Review (Get Document Endpoint)

⸻

🔹 Endpoint Details

Property	Value
Endpoint	GET /esignatureevents/{eventId}/documentpackage/{documentId}
Consumes	application/json
Current Produces	binary/octet-stream, application/pdf, application/json
Proposed Produces	application/pdf, application/json


⸻

⚙️ Current Behavior Overview

The Get Document endpoint retrieves an individual document from an eSignature event.
It currently declares three produces types — binary/octet-stream, application/pdf, and application/json.
However, AESIG does not explicitly set an Accept header in code. The media type negotiation is purely handled by Spring MVC content negotiation rules.

Here’s the current observed behavior:
	1.	No Accept header:
Spring selects the first entry (binary/octet-stream) from the produces list.
The document is returned as a binary response, visible as encoded bytes when viewed in raw mode.
	2.	Accept: binary/octet-stream:
AESIG returns binary bytes (PDF file stream) — typically consumed as file download.
	3.	Accept: application/pdf:
AESIG returns the document as a valid PDF file (browser-friendly).
	4.	Accept: application/json:
AESIG returns metadata or an encoded structure of the same document in JSON form.
	5.	The content (binary vs JSON) slightly differs due to encoding — the raw stream and the base64/structured JSON payload differ in size and encoding format.

Because the AESIG endpoint already produces valid PDFs (Content-Disposition: attachment; filename=...),
the binary/octet-stream support is redundant and less descriptive than application/pdf.

⸻

📊 Behavior Matrix – Before and After Change

Scenario	Current Behavior	Response Example	After Removing binary/octet-stream	Impact
Accept: application/pdf	✅ 200 OK	Content-Type: application/pdf	✅ 200 OK	None
Accept: application/json	✅ 200 OK	Content-Type: application/json	✅ 200 OK	None
Accept: binary/octet-stream	✅ 200 OK	Content-Type: binary/octet-stream	❌ 406 Not Acceptable	Must update to application/pdf
No Accept header	Defaults to binary/octet-stream	Content-Type: binary/octet-stream	Defaults to application/pdf	Validate client-side file handling logic
Accept: */*	✅ 200 OK	Defaults to binary stream	✅ 200 OK	Will default to application/pdf after change


⸻

💡 Root Cause / Reason for Change
	•	AESIG correctly generates and serves documents in PDF format, not raw binary.
	•	The binary/octet-stream header is too generic and does not accurately represent the response.
	•	Keeping it leads to inconsistent behavior when clients omit Accept.
	•	Removing it improves clarity, browser compatibility, and downstream integration reliability.

⸻

📦 Example Response Snapshots

--- Before Change ---
Request: GET /esignatureevents/{eventId}/documentpackage/{documentId}
Accept: (not provided)
Response: 200 OK
Content-Type: binary/octet-stream
Content-Disposition: attachment; filename=1.pdf
Content-Length: 335819

Request: GET /esignatureevents/{eventId}/documentpackage/{documentId}
Accept: binary/octet-stream
Response: 200 OK
Content-Type: binary/octet-stream
Body: Encoded binary stream (raw PDF bytes)

Request: GET /esignatureevents/{eventId}/documentpackage/{documentId}
Accept: application/pdf
Response: 200 OK
Content-Type: application/pdf
Body: PDF file content rendered in browser

Request: GET /esignatureevents/{eventId}/documentpackage/{documentId}
Accept: application/json
Response: 200 OK
Content-Type: application/json
Body: JSON metadata (document structure or encoded representation)

--- After Change ---
Request: GET /esignatureevents/{eventId}/documentpackage/{documentId}
Accept: (not provided)
Response: 200 OK
Content-Type: application/pdf
Content-Disposition: attachment; filename=1.pdf

Request: GET /esignatureevents/{eventId}/documentpackage/{documentId}
Accept: binary/octet-stream
Response: 406 Not Acceptable

Request: GET /esignatureevents/{eventId}/documentpackage/{documentId}
Accept: application/pdf
Response: 200 OK
Content-Type: application/pdf


⸻

🧠 Consumer Impact Summary

Consumer Type	Description	Action Required
✅ PDF Consumers	Already using Accept: application/pdf	No change
✅ JSON Consumers	Using Accept: application/json	No change
⚠ No Accept Header Consumers	Will now receive PDF (application/pdf) instead of binary stream	Validate file parsing or download handling
❌ Binary Consumers	Using Accept: binary/octet-stream	Must update header to application/pdf


⸻

✅ Recommendation
	•	Remove binary/octet-stream from the produces list.
	•	Keep application/pdf and application/json for structured and file-based retrieval.
	•	Communicate to any downstream consumers using binary accept headers to migrate to PDF.
	•	Test through Postman to confirm PDF renders successfully under application/pdf.

⸻

🚀 Rollout Plan

Phase	Action	Timeline
1️⃣	Review API Gateway logs for Accept: binary/octet-stream usage	Week 1
2️⃣	Deploy change to DEV/UAT and test with PDF rendering	Week 2
3️⃣	Notify consumers and share validation guide	Week 3
4️⃣	Promote to PROD after UAT validation and sign-off	Week 4


⸻

✅ End Result:
The Get Document endpoint will produce only application/pdf and application/json responses,
aligning with AESIG’s downstream integrations, improving clarity, and eliminating the use of ambiguous binary response types.

⸻

Would you like me to now compile all four endpoints into one combined Confluence master page (with internal navigation links like a mini index),
or would you prefer I wait until we also generate the Consumer Notification Email section later and include that at the end of the master page?