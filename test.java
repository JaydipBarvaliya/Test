Excellent 👍 — I’ve reviewed the screenshots and your Notepad++ formatting style.
You want four standalone endpoint documents written exactly like your Apply Signature one — clean, detailed, easy to paste into Confluence or Notepad++, and rich in reasoning (not just summary).

Below are four full documents (without the email section yet).
Each includes:
	•	Endpoint details
	•	Current behavior overview
	•	Before/after behavior matrix
	•	Example response snapshots
	•	Consumer impact summary
	•	Recommendations
	•	Rollout plan

⸻

🟦 AESIG API – Response Media Type Review (Update Transaction Endpoint)

⸻

♦ Endpoint Details

Endpoint:
PATCH /esignatureevents/{eventId}

Consumes:
application/json

Current Produces:
text/plain, application/json

Proposed Change:
Remove text/plain from produces so that the endpoint only produces application/json.

⸻

✳ Current Behavior Overview

The AESIG “Update Transaction” endpoint currently supports both text/plain and application/json.
However, the service does not return any actual text body — only a 200 OK response with empty body.

If the consumer doesn’t send any Accept header, Spring automatically chooses the first produces type (text/plain).
If Accept: application/json is sent, response is JSON (empty).

Therefore, text/plain support is redundant and may cause inconsistent behavior between consumers.

⸻

📊 Behavior Matrix – Before and After Change

Scenario	Current Behavior	Response Example	After Removing text/plain	Impact
Client sends Accept: application/json	✅ 200 OK	Content-Type: application/json	✅ 200 OK	None
Client sends Accept: text/plain	✅ 200 OK	Content-Type: text/plain	❌ 406 Not Acceptable	Must update Accept header
Client sends no Accept header	Defaults to text/plain	Content-Type: text/plain	Defaults to application/json	Verify client parsing
AESIG internal calls	Always JSON	Content-Type: application/json	No change	None


⸻

💡 Root Cause / Reason for Change
	•	AESIG never returns a plain text body.
	•	JSON is the standard response format for all AESIG APIs.
	•	Retaining text/plain causes inconsistent content negotiation when Accept header is missing.

⸻

🧾 Example Response Snapshots

Postman Example – Current Behavior

Case	Request	Response
No Accept header	200 OK, Content-Type: text/plain, Content-Length: 0	
Accept: application/json	200 OK, Content-Type: application/json, Content-Length: 0	
Accept: text/plain	200 OK, Content-Type: text/plain, Content-Length: 0	

After Change

Case	Request	Response
No Accept header	200 OK, Content-Type: application/json, Content-Length: 0	
Accept: application/json	200 OK, Content-Type: application/json, Content-Length: 0	
Accept: text/plain	❌ 406 Not Acceptable	


⸻

🧩 Consumer Impact Summary

Type	Description	Action Required
✅ JSON Consumers	Already using Accept: application/json	No action required
⚠ No Accept Header Consumers	Default Content-Type will now be JSON	Validate JSON parsing
❌ Plain Text Consumers	Will receive 406	Must change Accept header


⸻

✅ Recommendation
	•	Proceed with removing text/plain from produces.
	•	Communicate to consumers to use application/json.
	•	Validate all internal AESIG calls remain unaffected.

⸻

📅 Rollout Plan

Phase	Action	Timeline
1	Review gateway logs for text/plain usage	Week 1
2	Deploy to DEV/UAT	Week 2
3	Notify consumers for testing	Week 3
4	Deploy to PROD	Week 4


⸻

⸻

🟦 AESIG API – Response Media Type Review (Delete Transaction Endpoint)

⸻

♦ Endpoint Details

Endpoint:
DELETE /esignatureevents/{eventId}

Consumes:
None

Current Produces:
text/plain, application/json

Proposed Change:
Remove text/plain from produces list. Keep only application/json.

⸻

✳ Current Behavior Overview

The Delete Transaction endpoint currently defaults to text/plain when no Accept header is sent.
However, the API only returns HTTP 200 with an empty body — same as Update Transaction.
Hence, supporting plain text adds no value and creates inconsistency.

⸻

📊 Behavior Matrix – Before and After Change

Scenario	Current Behavior	Response Example	After Removing text/plain	Impact
Accept: application/json	✅ 200 OK	Content-Type: application/json	✅ 200 OK	None
Accept: text/plain	✅ 200 OK	Content-Type: text/plain	❌ 406 Not Acceptable	Must switch to JSON
No Accept header	Defaults to text/plain	Content-Type: text/plain	Defaults to JSON	Verify parsing


⸻

💡 Root Cause / Reason for Change
	•	Endpoint does not produce human-readable text.
	•	Removing plain text ensures consistent API contract (JSON-only).

⸻

🧾 Example Response Snapshots

Before Change

Case	Request	Response
No Accept header	200 OK, Content-Type: text/plain, Content-Length: 0	
Accept: application/json	200 OK, Content-Type: application/json	
Accept: text/plain	200 OK, Content-Type: text/plain	

After Change

Case	Request	Response
No Accept header	200 OK, Content-Type: application/json	
Accept: application/json	200 OK, Content-Type: application/json	
Accept: text/plain	❌ 406 Not Acceptable	


⸻

🧩 Consumer Impact Summary

Type	Description	Action Required
✅ JSON Consumers	No change	None
⚠ No Accept header	Default Content-Type = JSON	Verify parsing
❌ Plain Text Consumers	Will receive 406	Update Accept header


⸻

✅ Recommendation
	•	Remove text/plain from produces.
	•	Inform all consumers to use JSON Accept header.
	•	Validate response consistency in lower environments.

⸻

📅 Rollout Plan

Phase	Action	Timeline
1	Identify plain text usage in gateway logs	Week 1
2	Deploy JSON-only version to UAT	Week 2
3	Notify consumers	Week 3
4	Release to PROD	Week 4


⸻

⸻

🟦 AESIG API – Response Media Type Review (Apply Signature Endpoint)

⸻

(This is your reference document already, keeping unchanged)
I’ll skip re-pasting it here since it’s identical to your Notepad++ version, but the final package will include this one verbatim.

⸻

⸻

🟦 AESIG API – Response Media Type Review (Get Document Endpoint)

⸻

♦ Endpoint Details

Endpoint:
GET /esignatureevents/{eventId}/documentpackage/{documentId}

Consumes:
None

Current Produces:
binary/octet-stream, application/pdf, application/json

Proposed Change:
Remove binary/octet-stream from produces list. Keep only application/pdf and application/json.

⸻

✳ Current Behavior Overview

The “Get Document” endpoint returns the signed PDF document.
When no Accept header is provided, Spring defaults to the first produces value (binary/octet-stream).
This results in the same PDF being downloaded but marked as generic binary data.

If the client specifies Accept: application/pdf, the same PDF is returned but correctly recognized as a PDF.
Accept: application/json returns metadata (JSON).

Since AESIG always serves PDFs, the binary/octet-stream type is unnecessary.

⸻

📊 Behavior Matrix – Before and After Change

Scenario	Current Behavior	Response Example	After Removing binary/octet-stream	Impact
Accept: application/pdf	✅ 200 OK	Content-Type: application/pdf	✅ 200 OK	None
Accept: binary/octet-stream	✅ 200 OK	Content-Type: binary/octet-stream	❌ 406 Not Acceptable	Must update Accept
Accept: application/json	✅ 200 OK	Metadata JSON	✅ 200 OK	None
No Accept header	Defaults to binary/octet-stream	Content-Type: binary/octet-stream	Defaults to application/pdf	Verify MIME expectations


⸻

💡 Root Cause / Reason for Change
	•	PDF is a specific MIME type; binary/octet-stream is generic.
	•	Consumers downloading files will experience better browser compatibility and inline preview.
	•	Defaulting to application/pdf improves semantic accuracy.

⸻

🧾 Example Response Snapshots

Before Change

Case	Request	Response
No Accept header	200 OK, Content-Type: binary/octet-stream	
Accept: application/pdf	200 OK, Content-Type: application/pdf	
Accept: application/json	200 OK, Content-Type: application/json	
Accept: binary/octet-stream	200 OK, Content-Type: binary/octet-stream	

After Change

Case	Request	Response
No Accept header	200 OK, Content-Type: application/pdf	
Accept: application/pdf	200 OK, Content-Type: application/pdf	
Accept: application/json	200 OK, Content-Type: application/json	
Accept: binary/octet-stream	❌ 406 Not Acceptable	


⸻

🧩 Consumer Impact Summary

Type	Description	Action Required
✅ PDF Consumers	No change	None
⚠ No Accept header Consumers	Default MIME now = PDF	Validate preview behavior
❌ binary/octet-stream Consumers	Will receive 406	Remove header or switch to PDF


⸻

✅ Recommendation
	•	Proceed with removing binary/octet-stream.
	•	Notify consumers using binary MIME to migrate to application/pdf.
	•	Monitor gateway logs for MIME usage.

⸻

📅 Rollout Plan

Phase	Action	Timeline
1	Analyze gateway logs for binary MIME usage	Week 1
2	Deploy updated version to DEV/UAT	Week 2
3	Consumer testing and confirmation	Week 3
4	Deploy to PROD	Week 4


⸻

⸻

Would you like me to combine these four (formatted like your Notepad++ version) into one downloadable .txt or .docx file, so you can paste them into Confluence or Notepad without formatting loss?
It’ll preserve the same visual layout you liked in your Apply Signature page.