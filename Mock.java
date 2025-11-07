Perfect ✅ — that’s actually the best structure for Confluence: one page per endpoint, cleanly separated and easy to link from a master summary page.

Below are four standalone Confluence-ready documents, each one fully detailed, consistent in structure, and ready to paste as its own page.
(Each includes: purpose, current behavior, proposed change, impact matrix, consumer actions, and rollout plan.)

⸻

🧾 1️⃣ AESIG – Update Transaction Endpoint (PATCH /esignatureevents/{eventId})

⸻

🔹 Purpose

To standardize the response type for the Update Transaction API.
Currently, the endpoint supports both text/plain and application/json, though no actual text response is returned.

⸻

⚙️ Endpoint Details

Property	Value
HTTP Method	PATCH
Path	/esignatureevents/{eventId}
Consumes	application/json
Current Produces	text/plain, application/json
Proposed Produces	application/json


⸻

🧩 Current Behavior
	•	If no Accept header → defaults to text/plain.
	•	If Accept: application/json → returns JSON.
	•	Response body is empty; only 200 OK.

⸻

🔁 Proposed Change

Remove text/plain from produces, keeping only application/json.

⸻

📊 Behavior Comparison

Scenario	Current Behavior	After Change	Impact
Accept: application/json	✅ 200 OK	✅ 200 OK	None
Accept: text/plain	✅ 200 OK	❌ 406 Not Acceptable	Must use JSON
No Accept header	Defaults to text/plain	Defaults to application/json	Verify consumer parsing logic


⸻

🧠 Rationale
	•	AESIG does not return any plain text body.
	•	JSON is the standard for API responses.
	•	Removes ambiguity for clients parsing empty text.

⸻

✅ Consumer Action
	•	Ensure requests send Accept: application/json or omit the header.
	•	Stop sending text/plain.

⸻

🗓️ Rollout Plan

Step	Action
1	Validate logs for clients using text/plain
2	Update to JSON-only in DEV/UAT
3	Notify consumers
4	Deploy to PROD after validation


⸻

⸻

🧾 2️⃣ AESIG – Delete Transaction Endpoint (DELETE /esignatureevents/{eventId})

⸻

🔹 Purpose

To align Delete Transaction response type with JSON-only convention and remove redundant text/plain.

⸻

⚙️ Endpoint Details

Property	Value
HTTP Method	DELETE
Path	/esignatureevents/{eventId}
Consumes	—
Current Produces	text/plain, application/json
Proposed Produces	application/json


⸻

🧩 Current Behavior
	•	No Accept header → defaults to text/plain.
	•	Accept: application/json → returns JSON (empty body).

⸻

🔁 Proposed Change

Keep only application/json.

⸻

📊 Behavior Comparison

Scenario	Current Behavior	After Change	Impact
Accept: application/json	✅ 200 OK	✅ 200 OK	None
Accept: text/plain	✅ 200 OK	❌ 406 Not Acceptable	Must switch to JSON
No Accept header	Defaults to text/plain	Defaults to application/json	Minimal impact


⸻

🧠 Rationale
	•	Endpoint does not produce any plain text content.
	•	Aligns with REST standards and AESIG’s JSON standardization.

⸻

✅ Consumer Action
	•	Stop using text/plain.
	•	Prefer Accept: application/json (or omit header).

⸻

🗓️ Rollout Plan

Step	Action
1	Review logs for consumers using text/plain
2	Apply fix in DEV/UAT
3	Notify consumers for validation
4	Deploy to PROD post sign-off


⸻

⸻

🧾 3️⃣ AESIG – Apply Signature Endpoint (POST /esignatureevents/{eventId}/parties/{partyId}/signs)

⸻

🔹 Purpose

To remove the unused binary response type and standardize the endpoint’s response to JSON.

⸻

⚙️ Endpoint Details

Property	Value
HTTP Method	POST
Path	/esignatureevents/{eventId}/parties/{partyId}/signs
Consumes	application/json
Current Produces	binary/octet-stream, application/json
Proposed Produces	application/json


⸻

🧩 Current Behavior
	•	AESIG does not return any binary data; only 200 OK.
	•	Internally, AESIG → OneSpan calls always use Accept: application/json.

⸻

🔁 Proposed Change

Remove binary/octet-stream from produces.

⸻

📊 Behavior Comparison

Scenario	Current Behavior	After Change	Impact
Accept: application/json	✅ 200 OK	✅ 200 OK	None
Accept: binary/octet-stream	✅ 200 OK	❌ 406 Not Acceptable	Must use JSON
No Accept header	Defaults to binary/octet-stream	Defaults to application/json	Minimal impact


⸻

🧠 Rationale
	•	No binary payload is returned.
	•	OneSpan accepts JSON only.
	•	Standardizes behavior and simplifies integration.

⸻

✅ Consumer Action
	•	Use Accept: application/json.
	•	Do not rely on binary/octet-stream.

⸻

🗓️ Rollout Plan

Step	Action
1	Check logs for consumers using binary/octet-stream
2	Implement change in DEV/UAT
3	Notify consumers
4	Deploy to PROD after validation


⸻

⸻

🧾 4️⃣ AESIG – Get Document Endpoint (GET /esignatureevents/{eventId}/documentpackage/{documentId})

⸻

🔹 Purpose

To remove redundant binary/octet-stream support and align file download responses with the proper MIME type application/pdf.

⸻

⚙️ Endpoint Details

Property	Value
HTTP Method	GET
Path	/esignatureevents/{eventId}/documentpackage/{documentId}
Consumes	—
Current Produces	binary/octet-stream, application/pdf, application/json
Proposed Produces	application/pdf, application/json


⸻

🧩 Current Behavior
	•	No Accept header → defaults to binary/octet-stream.
	•	Accept: binary/octet-stream → returns PDF file as generic binary stream.
	•	Accept: application/pdf → returns same file but with correct PDF MIME.
	•	Accept: application/json → returns metadata or stats as JSON.

⸻

🔁 Proposed Change

Remove binary/octet-stream from produces.
Keep only application/pdf (for files) and application/json (for metadata).

⸻

📊 Behavior Comparison

Scenario	Current Behavior	After Change	Impact
Accept: application/pdf	✅ Returns file (Content-Type: application/pdf)	✅ Same	None
Accept: application/json	✅ Returns metadata JSON	✅ Same	None
Accept: binary/octet-stream	✅ Returns file (generic MIME)	❌ 406 Not Acceptable	Must switch to PDF or remove header
No Accept header	Defaults to binary/octet-stream	Defaults to application/pdf	✅ File still downloads, MIME is more accurate


⸻

🧠 Rationale
	•	application/pdf is the correct and explicit MIME type for PDFs.
	•	binary/octet-stream is generic and redundant.
	•	Consumers that don’t send Accept will continue to work (fallback = first produces type).
	•	This change improves browser compatibility, MIME clarity, and REST compliance.

⸻

✅ Consumer Action
	•	If sending Accept: binary/octet-stream, either:
	•	Change to Accept: application/pdf, or
	•	Remove Accept header (API will default to application/pdf).
	•	No change needed for JSON metadata consumers.

⸻

📥 Example Before vs After

Case	Before	After
Header	Accept: binary/octet-stream	Accept: application/pdf
Response	200 OK, Content-Type: binary/octet-stream	200 OK, Content-Type: application/pdf
File	Same PDF content	Same PDF content
Browser Behavior	Prompts generic download	Opens PDF viewer inline


⸻

🗓️ Rollout Plan

Step	Action
1	Identify consumers sending binary/octet-stream
2	Notify them of deprecation
3	Apply change to DEV/UAT
4	Deploy to PROD after validation


⸻

🧭 Summary

Impacted Consumers	Resolution
Sending Accept: binary/octet-stream	Update to application/pdf or omit header
Sending Accept: application/pdf	No change
Sending Accept: application/json	No change
Sending no Accept	Still works (defaults to PDF)


⸻

✅ End Result:
	•	Standardized media types across AESIG endpoints
	•	Consistent JSON or PDF responses
	•	Predictable defaults and improved interoperability

⸻

Would you like me to also generate a “master index page” for Confluence that summarizes and links to all four of these pages (with short one-line summaries and impact tags)?
That’s often what leads/managers prefer at the top level.