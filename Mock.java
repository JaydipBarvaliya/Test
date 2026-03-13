Below is clean Confluence / Jira ready documentation.
Everything uses standard Markdown tables, which Confluence and Jira both convert properly when pasted.

You can copy-paste directly.

⸻

Admin Reprocess API Documentation

Overview

The Admin Ingest API allows operations teams to manually trigger the reprocessing of ingestion transactions that previously failed.

This endpoint is primarily used for operational recovery scenarios when a transaction needs to be retried without waiting for automated retries or when manual intervention is required.

The API performs the following operations:
	1.	Accepts a list of ingestion transaction IDs.
	2.	Identifies duplicates in the request.
	3.	Verifies which transactions exist in the database.
	4.	Determines which transactions are eligible for reprocessing (STATUS = FAILURE).
	5.	Updates eligible transactions from FAILURE → ERROR.
	6.	Resets RETRY_COUNT to 0 so the scheduler can retry them.
	7.	Returns detailed response metrics for operational visibility.

⸻

Endpoint Details

Attribute	Value
HTTP Method	PUT
Endpoint	/admin/ingest
Purpose	Reprocess failed ingestion transactions
Authentication	Internal Admin API
Triggered By	Operations / Support teams


⸻

Request Body

Structure

Field	Type	Required	Description
txnsToReprocess	List	Yes	List of ingestion transaction IDs that should be reprocessed


⸻

Example Request

{
  "txnsToReprocess": [
    "18adffed-ae96-4bb3-b866-3047a6e97b1e",
    "19adffed-ae96-4bb3-b866-3047a6e97b1e",
    "20adffed-ae96-4bb3-b866-3047a6e97b1e",
    "21adffed-ae96-4bb3-b866-3047a6e97b1e",
    "919ba9cb-6c10-4407-a5d2-2876cb93ca93",
    "919ba9cb-6c10-4407-a5d2-2876cb93ca93"
  ]
}

Note that duplicate transaction IDs may appear in the request.

⸻

Response Body

The response provides detailed metrics about how each transaction was processed.

⸻

Response Fields

Field	Type	Description
requestedTxnCount	Integer	Total transaction IDs received in request
successCount	Integer	Number of transactions successfully reprocessed
notFoundCount	Integer	Transactions that do not exist in the database
duplicateCount	Integer	Duplicate transaction IDs detected in request
otherThanFailureStatusCount	Integer	Transactions found but not eligible for reprocessing
successTxnIds	List	Successfully reprocessed transaction IDs
notFoundTxnIds	List	Transaction IDs not found in database
duplicateTxnIds	List	Duplicate transaction IDs from request
otherThanFailureStatusTxnIds	List	Transactions that were not in FAILURE state


⸻

Example Response

{
  "requestedTxnCount": 6,
  "successCount": 2,
  "notFoundCount": 3,
  "duplicateCount": 1,
  "otherThanFailureStatusCount": 1,
  "successTxnIds": [
    "18adffed-ae96-4bb3-b866-3047a6e97b1e",
    "19adffed-ae96-4bb3-b866-3047a6e97b1e"
  ],
  "notFoundTxnIds": [
    "20adffed-ae96-4bb3-b866-3047a6e97b1e",
    "21adffed-ae96-4bb3-b866-3047a6e97b1e",
    "22adffed-ae96-4bb3-b866-3047a6e97b1e"
  ],
  "duplicateTxnIds": [
    "919ba9cb-6c10-4407-a5d2-2876cb93ca93"
  ],
  "otherThanFailureStatusTxnIds": [
    "18adffed-ae96-4bb3-b866-3047a6e97b1e"
  ]
}


⸻

Transaction Processing Logic

The API processes transactions using the following steps:

Step	Description
1	Receive transaction IDs from request
2	Identify duplicate IDs in request
3	Remove duplicates for processing
4	Fetch existing transactions from database
5	Identify transactions not present in database
6	Identify transactions with status FAILURE
7	Identify transactions with other statuses
8	Update eligible transactions (FAILURE → ERROR)
9	Reset retry count to 0
10	Return aggregated response metrics


⸻

Database Table

STOR_INGEST_TXN

Column	Type	Description
INGEST_TXN_ID	VARCHAR2(36)	Unique ingestion transaction identifier
LOB_ID	VARCHAR2(50)	Line of business identifier
TRACEABILITY_ID	VARCHAR2(50)	Request trace identifier
DGVL_DRAWER_ID	VARCHAR2(50)	Drawer identifier
DGVL_FOLDER_ID	VARCHAR2(50)	Folder identifier
DGVL_FILE_TOKEN	VARCHAR2(50)	File token
DGVL_FILE_NAME	VARCHAR2(50)	File name
NAS_PATH	VARCHAR2(50)	NAS storage path
STOR_TXN_ID	VARCHAR2(50)	Storage transaction identifier
STOR_FILE_ID	VARCHAR2(50)	File identifier
STOR_FILE_NAME	VARCHAR2(50)	Storage file name
STOR_CONFIG_ID	NUMBER	Configuration reference
STATUS	VARCHAR2(50)	Transaction status
STATE	VARCHAR2(50)	Processing state
RETRY_COUNT	NUMBER	Retry attempt counter
CREATION_DTTM	TIMESTAMP	Record creation timestamp
LAST_UPDATE_DTTM	TIMESTAMP	Last update timestamp


⸻

Database Queries

1. Fetch Existing Transactions

SELECT INGEST_TXN_ID
FROM STOR_INGEST_TXN
WHERE INGEST_TXN_ID IN (:txnIds)

Purpose:

Determines which transaction IDs exist in the system.

⸻

2. Fetch Transactions in FAILURE Status

SELECT INGEST_TXN_ID
FROM STOR_INGEST_TXN
WHERE INGEST_TXN_ID IN (:txnIds)
AND STATUS = 'FAILURE'

Purpose:

Identifies transactions eligible for reprocessing.

⸻

3. Bulk Update Transactions

UPDATE STOR_INGEST_TXN
SET STATUS = 'ERROR',
    RETRY_COUNT = 0,
    LAST_UPDATE_DTTM = SYSTIMESTAMP
WHERE INGEST_TXN_ID IN (:txnIds)
AND STATUS = 'FAILURE'

Purpose:
	•	Moves transactions from FAILURE to ERROR
	•	Resets retry count to allow scheduler retry
	•	Updates audit timestamp

⸻

Status Transition

Current Status	Action	New Status
FAILURE	Admin Reprocess API	ERROR
ERROR	Scheduler Retry	PROCESSING
PROCESSING	Successful ingestion	SUCCESS
ERROR	Retry limit reached	PERMANENT_FAILURE


⸻

Logging Strategy

Two logs are generated during execution.

API Invocation

Admin ingest API invoked with {txnCount} txnIds. traceabilityId={traceId}

API Completion

Admin ingest completed. requested={count}, success={count}, notFound={count}, duplicates={count}, otherThanFailure={count}

These logs allow operations teams to quickly identify processing results.

⸻

Operational Notes

Scenario	Behavior
Duplicate IDs in request	Returned in duplicateTxnIds
Transaction not found	Returned in notFoundTxnIds
Transaction already processed	Returned in otherThanFailureStatusTxnIds
Valid failed transaction	Updated to ERROR
Retry count	Reset to 0


⸻

Scheduler Interaction

After the admin API updates transactions:

STATUS = ERROR
RETRY_COUNT = 0

The ingestion scheduler will pick up these records and restart retry attempts.

⸻

Performance Considerations

Area	Implementation
Database calls	Bulk queries used
Duplicate detection	In-memory using Set
Updates	Single bulk update
Indexing	INGEST_TXN_ID should be indexed


⸻

Recommended Index

CREATE INDEX IDX_STOR_INGEST_TXN_ID
ON STOR_INGEST_TXN (INGEST_TXN_ID);

This ensures fast lookups for bulk transaction queries.

⸻

Summary

The Admin Reprocess API provides a controlled mechanism to manually restart failed ingestion transactions while maintaining operational visibility and data integrity.

Key capabilities include:
	•	Duplicate detection
	•	Transaction existence validation
	•	Controlled status transitions
	•	Retry counter reset
	•	Detailed operational response reporting
	•	Bulk database operations for performance

⸻

If you’d like, I can also give you a second section for Confluence called:

“Sequence Diagram: Admin Reprocess Flow”

That visual diagram makes this instantly understandable for architects and reviewers.