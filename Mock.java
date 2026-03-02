GET Transaction Status (Ingest) ✅

Endpoint
	•	Method: GET
	•	Path: /ingest/status/{ingestTxnId}
	•	Operation: ingest.status

What it does (high-level)

This endpoint returns the current status + state of an ingestion transaction using the ingestTxnId.
It basically:
	1.	Validates ingestTxnId is not blank
	2.	Looks up the transaction in DB (via txnRepo.findByIngestTxnId(ingestTxnId))
	3.	Returns the mapped response (IngestTxnRs) if found
	4.	Throws an error if not found / invalid input

⸻

Request

URL (example)

GET {{baseUrl}}/ingest/status/{{ingestTxnId}}

Path Parameter
	•	ingestTxnId (required)
Unique ingestion transaction identifier.

Headers (required in your flow)
	•	Authorization: Bearer {{dgvlmToken}}
	•	lobId: {{lobid}}
	•	traceabilityID: {{traceabilityID}}
	•	Accept: application/json

Request Body: None (this is a GET)

⸻

Success Response (200)

Response Body (IngestTxnRs) – example

{
  "ingestTxnId": "84684cc7-6f58-4a64-916b-ad59ace58fa4",
  "lobId": "tdi",
  "status": "ACTIVE",
  "state": "FN_BATCH_TRIGGERED",
  "retryCount": 0,
  "creationDttm": "2026-03-02T11:45:15.451029Z",
  "lastUpdatedDttm": "2026-03-02T11:45:16.619729Z",
  "digitalVault": {
    "drawerId": "....",
    "folderId": "....",
    "fileToken": "this value we get from the DGVLA",
    "fileName": "..."
  },
  "storage": {
    "fileId": "....",
    "txnId": "....",
    "fileName": "..."
  }
}


⸻

Status / State meanings (what consumers should understand)

status (overall outcome)

Valid values:
	•	ACTIVE → transaction is currently being processed
	•	SUCCESS → transaction completed successfully
	•	ERROR → transaction is in error state and requires error-handling flow
	•	FAILURE → transaction failed and max retry count has been reached

state (where in the pipeline)

Valid values (current contract):
	•	RECEIVED → request received + validations done, next step is to call FileNet BatchDoc API
	•	FN_BATCH_TRIGGERED → FileNet BatchDoc API triggered, next step is to await Kafka message
	•	DGVL_PUSHING → Kafka message received, pushing file to DGVL via DGVLA
	•	DGVL_COMPLETE → push to DGVL completed, next step is delete file from NAS
	•	COMPLETE → all necessary steps completed

⸻

Error Responses (high-level)

Your OpenAPI lists these:
	•	400 Bad Request (ex: blank ingestTxnId, or “transaction not found” based on current service behavior)
	•	401 Unauthorized (missing/invalid token)
	•	403 Forbidden (token valid but not permitted)
	•	404 Not Found (defined in spec; depending on implementation you may currently throw 400 for “not found”)
	•	405 Method Not Allowed
	•	500 Internal Server Error
	•	503 Service Unavailable

⸻

Edge cases + things to remember ⚠️
	•	Blank / null ingestTxnId: should fail fast (you’re already validating this).
	•	Transaction not found: currently your service throws an exception (looks like 400 in your code path).
	•	If you want REST-purity, 404 is cleaner. But document what you actually return today.
	•	Logging: always sanitize ingestTxnId before logging (you’re using LogSanitizeUtil.sanitizeLogObj(...) ✅).
	•	Response consistency: creationDttm and lastUpdatedDttm are date-time strings. Clients should treat them as UTC timestamps.
	•	digitalVault.fileToken: populated from DGVLA (so clients shouldn’t assume it exists until you reach the DV-related state).
	•	storage.*: may be partially present depending on where the transaction is in the flow (early states might not have all IDs yet).

⸻

If you paste the exact request you’re sending (Postman raw details are fine), I’ll align the doc 1:1 with your actual header names/casing (ex: traceabilityID vs traceabilityId) so you don’t get pinged in review.