You’re actually very close already. The core idea is correct, but the response model in your OpenAPI spec is too weak for what the API is actually doing. Right now it only returns:
	•	reprocessCount
	•	successfulReprocessTxnIds

But your real business logic has three outcomes, not one:
	1.	Transaction exists and moved FAILURE → ERROR
	2.	Transaction does not exist
	3.	Transaction exists but cannot be updated (optional but recommended)

Let’s cleanly design this.

⸻

1. First fix the OpenAPI Response Schema

Your response should explicitly communicate what happened to each ID.

Instead of only:

{
  "reprocessCount": 4,
  "successfulReprocessTxnIds": []
}

You should return something like:

{
  "requestedTxnCount": 30,
  "successCount": 20,
  "notFoundCount": 10,
  "successTxnIds": [],
  "notFoundTxnIds": []
}

Optional but recommended:

"skippedTxnIds"

(for transactions that are not in FAILURE state)

⸻

2. Updated OpenAPI Schema

Modify adminIngestRs.

"adminIngestRs": {
  "description": "Response for admin ingest reprocess API.",
  "type": "object",
  "properties": {

    "requestedTxnCount": {
      "type": "integer",
      "description": "Total transaction ids received in request."
    },

    "successCount": {
      "type": "integer",
      "description": "Number of transactions successfully moved from FAILURE to ERROR."
    },

    "notFoundCount": {
      "type": "integer",
      "description": "Number of transaction ids that do not exist in database."
    },

    "successTxnIds": {
      "type": "array",
      "description": "Transactions successfully moved to ERROR state.",
      "items": {
        "type": "string"
      }
    },

    "notFoundTxnIds": {
      "type": "array",
      "description": "Transaction ids not found in database.",
      "items": {
        "type": "string"
      }
    }
  }
}

Example:

"example": {
  "requestedTxnCount": 4,
  "successCount": 3,
  "notFoundCount": 1,
  "successTxnIds": [
    "18adffed-ae96-4bb3-b866-3047a6e97b1e",
    "19adffed-ae96-4bb3-b866-3047a6e97b1e",
    "20adffed-ae96-4bb3-b866-3047a6e97b1e"
  ],
  "notFoundTxnIds": [
    "21adffed-ae96-4bb3-b866-3047a6e97b1e"
  ]
}


⸻

3. Controller Logic (Spring Boot)

Your controller should roughly do this:

public AdminIngestResponse reprocessTransactions(List<String> txnIds) {

    List<String> successIds = new ArrayList<>();
    List<String> notFoundIds = new ArrayList<>();

    for (String txnId : txnIds) {

        Optional<IngestTxn> txn = repository.findByTxnId(txnId);

        if (txn.isEmpty()) {
            notFoundIds.add(txnId);
            continue;
        }

        if (txn.get().getStatus().equals(Status.FAILURE)) {

            repository.updateStatusToError(txnId);
            successIds.add(txId);

        }
    }

    return AdminIngestResponse.builder()
            .requestedTxnCount(txnIds.size())
            .successCount(successIds.size())
            .notFoundCount(notFoundIds.size())
            .successTxnIds(successIds)
            .notFoundTxnIds(notFoundIds)
            .build();
}


⸻

4. DO NOT update one by one in DB

This is the part where many people screw up performance.

Instead of:

select
update
select
update

You should batch query.

Step 1 — find existing IDs

SELECT ingest_txn_id
FROM store_config_ingest_txn
WHERE ingest_txn_id IN (:txnIds);


⸻

Step 2 — find not found

In Java:

notFoundIds = requestedIds - dbIds


⸻

Step 3 — update all valid ones

UPDATE store_config_ingest_txn
SET status = 'ERROR',
    retry_count = 0,
    last_update_dtdm = SYSTIMESTAMP
WHERE ingest_txn_id IN (:txnIds)
AND status = 'FAILURE';


⸻

5. Why FAILURE → ERROR

Just documenting the business logic clearly:

ERROR
 ↓ retry scheduler
ERROR retry 1
 ↓
ERROR retry 2
 ↓
ERROR retry 3
 ↓
FAILURE

Then ops fixes the file.

Then:

FAILURE
 ↓ admin API
ERROR
 ↓ scheduler
processing again

So admin API does NOT reprocess, it only moves status.

This is important for documentation.

⸻

6. Your Scheduler (future)

Scheduler simply runs:

SELECT *
FROM store_config_ingest_txn
WHERE status = 'ERROR'
AND retry_count < 3
FOR UPDATE SKIP LOCKED;

Then processes them.

⸻

7. Small improvement to your request schema

Your request body is fine but rename slightly:

Current:

txnsToReprocess

Better:

txnIds

Cleaner.

⸻

8. Final API Flow

OPS TEAM
   │
   │ call
   ▼
/admin/ingest

payload:
{
  "txnIds": []
}

API
 │
 │ validate ids
 │ update FAILURE → ERROR
 ▼

Response
{
  requestedTxnCount
  successTxnIds
  notFoundTxnIds
}


⸻

9. One more thing you should seriously add

Limit the request size.

Example:

max 500 txnIds per request

Otherwise someone will send 50k IDs and kill your DB.

⸻

If you want, I can also show you a much cleaner OpenAPI spec structure for this endpoint (yours can be simplified a lot). It will make your generated controller + models much cleaner.