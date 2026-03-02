Perfect 👍 this is exactly the kind of thing that separates “I wrote code” from “I designed a system.”

Below is a clean, structured, Confluence-ready documentation page for your DGVLM async error processing and retry architecture. You can paste this directly into GitHub Markdown or Confluence.

⸻

DGVLM – Async Error Processing & Retry Architecture

⸻

1. Overview

The DGVLM system integrates with the external BatchDoc API to trigger downstream document processing.

Since the external system can fail due to:
	•	Network errors
	•	Authentication issues
	•	API downtime
	•	Timeout exceptions

We implemented an asynchronous error handling and retry mechanism to ensure reliability and eventual consistency.

⸻

2. High-Level Architecture

There are two execution paths:

Flow	Trigger	Execution Mode
Initial Ingest	Ingest API (client call)	Asynchronous
Retry Processing	Scheduler	Synchronous


⸻

3. Initial Processing Flow (Async)

Step 1 – Ingest API Called

When a client calls the Ingest API:
	1.	Transaction record is saved in DB.
	2.	BatchDocAsyncService.triggerAsync() is invoked.
	3.	BatchDoc API call happens asynchronously.

Why Async?

We do not want the client request to block waiting for:
	•	External API response
	•	Network delay
	•	Downstream processing

⸻

Step 2 – Async Execution

BatchDocAsyncService
    → calls BatchDocService.triggerBatchDocAPI()

If BatchDoc API call succeeds:
	•	status = ACTIVE
	•	state = FN_BATCH_TRIGGERED
	•	Batch ID stored

If BatchDoc API call fails:
	•	status = ERROR
	•	state = RECEIVED
	•	Updated in DB
	•	Scheduler will pick it later

⸻

4. Retry Architecture (Scheduler-Based)

Scheduler Configuration

Configured using Spring:

@Scheduled(cron = "${filenet.batchdoc.retry.cron}")

Example application.properties:

filenet.batchdoc.retry.cron=0 0 * * * *
filenet.batchdoc.retry.batch-size=50

Cron Meaning

0 0 * * * * → Runs every hour.

⸻

5. Claim & Lock Mechanism

To prevent multiple JVMs from processing the same rows:

We use:

SELECT ... FOR UPDATE SKIP LOCKED

Why?

If two scheduler instances run at same time:
	•	Scheduler A locks first 50 ERROR rows
	•	Scheduler B skips those locked rows
	•	No duplicate processing

⸻

6. Retry Flow

Step 1 – Claim Rows

RetryClaimService.claimErrorTransactions(batchSize)
	•	Selects transactions where:
	•	status = ERROR
	•	state = RECEIVED
	•	Locks them
	•	Moves them to ACTIVE temporarily

⸻

Step 2 – Process Claimed Transactions

BatchDocRetryScheduler
    → BatchDocRetryService.process(txnId)

Retry execution is synchronous inside scheduler.

⸻

7. Retry Processing Logic

Inside BatchDocRetryService.process():

Case A – Success

If API call succeeds:
	•	status = ACTIVE
	•	state = FN_BATCH_TRIGGERED
	•	Retry count unchanged

⸻

Case B – Failure

If API call fails:
	1.	retryCount++
	2.	If retryCount >= failureRetryMax:
	•	status = FAILURE
	3.	Else:
	•	status = ERROR
	•	state = RECEIVED
	•	Will be picked up next run

⸻

8. Retry Configuration

Retry Max

Loaded dynamically from configuration:

@PostConstruct
public void init() {
    this.failureRetryMax = Integer.parseInt(
        configurationProperties.getConfigProperty("failure.retry.max")
    );
}

Why Dynamic?
	•	Can change retry policy without code change
	•	Can tune per environment

⸻

Batch Size

Loaded from application properties:

filenet.batchdoc.retry.batch-size=50

This controls:
	•	How many failed transactions are processed per scheduler run.

⸻

9. State Machine

Transaction lifecycle:

RECEIVED + ERROR
    ↓ (Scheduler claims)
ACTIVE
    ↓
FN_BATCH_TRIGGERED

Failure scenario:

ERROR + RECEIVED
    ↓ (Retry)
ERROR + RECEIVED (if retry < max)
    ↓
FAILURE (if retry >= max)


⸻

10. Why Async for Initial, Sync for Retry?

Scenario	Mode	Reason
Client Call	Async	Non-blocking API
Scheduler	Sync	Controlled batch processing

Scheduler does not need async because:
	•	It is already background
	•	It processes in controlled batch size
	•	Easier transactional control

⸻

11. Concurrency Control

We prevent duplicate processing using:
	•	FOR UPDATE SKIP LOCKED
	•	Transaction boundaries
	•	Status transition from ERROR → ACTIVE
	•	Commit-based lock release

⸻

12. Commit & Lock Behavior

Lock exists only during DB transaction.

Flow:
	1.	Rows locked
	2.	Status changed
	3.	Transaction committed
	4.	Lock released

Lock prevents:
	•	Simultaneous processing

Status change prevents:
	•	Future duplicate reprocessing

⸻

13. Logging Strategy

Logging levels:

Event	Level
Scheduler start	INFO
No transactions found	INFO
Retry success	INFO
Retry failure	ERROR
Retry exhausted	WARN
API request building	DEBUG


⸻

14. Failure Handling Philosophy

We designed system to be:
	•	Eventually consistent
	•	Retry-safe
	•	Idempotent-aware
	•	Distributed-safe
	•	Configurable

⸻

15. End-to-End Flow Summary

Initial Failure

Client → Async → API fails → Mark ERROR → Scheduler picks later

Retry Failure

Scheduler → Retry → Fails → retryCount++ → ERROR or FAILURE

Retry Success

Scheduler → Retry → Success → ACTIVE + FN_BATCH_TRIGGERED

⸻

16. Design Goals Achieved
	•	Non-blocking API
	•	Resilient retry
	•	Multi-instance safe
	•	Configurable retry policy
	•	Controlled batch processing
	•	Clear state transitions
	•	Observable logging

⸻

17. Future Improvements (Optional)
	•	Add optimistic locking (@Version)
	•	Add exponential backoff
	•	Add dead-letter table
	•	Add metrics (Micrometer)
	•	Add correlation ID tracing

⸻

Final Architecture Summary

This implementation ensures:
	•	Reliability of external integration
	•	No duplicate processing
	•	Controlled retry behavior
	•	Clear transaction state lifecycle
	•	Production-grade observability

⸻

If you want, I can now also generate:
	•	A system design diagram (Mermaid or PlantUML)
	•	Sequence diagram
	•	State transition diagram
	•	Or interview explanation version (how to explain this in 3 minutes)

Tell me which one you want next.