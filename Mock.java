Below is a clean, Confluence-style documentation you can directly use for your DigiVLM project. I kept it structured, concise, and engineering-focused, the way backend teams typically document retention jobs.

⸻

Transaction Retention Cleanup Process

1. Overview

The Transaction Retention Cleanup Process is implemented to automatically remove completed transactions from the STOR_INGEST_TXN table after a configured retention period.

The purpose of this process is to:
	•	Prevent uncontrolled growth of the transaction table.
	•	Maintain database performance.
	•	Retain only relevant operational data for troubleshooting and monitoring.
	•	Ensure completed transactions are removed safely after a defined time window.

The cleanup is performed through a scheduled job that runs weekly and deletes transactions that have been completed and inactive beyond the configured retention period.

⸻

2. Retention Strategy

The retention logic is based on the last update timestamp (LAST_UPDATE_DTTM) rather than the creation timestamp.

Why LAST_UPDATE_DTTM is Used

Transactions in the STOR_INGEST_TXN table pass through multiple processing stages including retries and status updates. Because of this lifecycle:
	•	A transaction may be created at time T1.
	•	It may go through multiple retries and state changes.
	•	The transaction may only be successfully completed at a later time T2.

If retention were calculated using CREATION_DTTM, the system might delete transactions that are still being retried or processed.

Using LAST_UPDATE_DTTM ensures:
	•	Retention period starts after the transaction has fully completed.
	•	Active or retrying transactions are never deleted prematurely.
	•	Cleanup reflects the true lifecycle completion time.

Example lifecycle:

Date	Event
March 1	Transaction created
March 3	Retry attempt
March 7	Retry attempt
March 15	Processing completed successfully

In this case:

LAST_UPDATE_DTTM = March 15

Retention countdown begins from March 15, not March 1.

⸻

3. Transaction Completion Criteria

Transactions are only eligible for deletion when they have reached their final completed state.

The system verifies completion using two fields:

Column	Required Value	Meaning
STATUS	SUCCESS	All processing steps completed successfully
STATE	COMPLETE	Associated file has been successfully removed from NAS

Final Transaction State

A transaction is considered fully completed only when:

STATUS = 'SUCCESS'
STATE  = 'COMPLETE'

This ensures:
	•	The transaction processing workflow has finished.
	•	Associated files have already been cleaned up from the NAS drive.
	•	The transaction is no longer required by the system.

Only transactions satisfying both conditions are considered for retention cleanup.

⸻

4. Retention Period Configuration

The retention period is not hardcoded in the application.

Instead, it is stored in the configuration table and loaded dynamically at runtime.

Example configuration:

Configuration Key	Value
TXN_RETENTION_DAYS	15

This configuration allows:
	•	Operations teams to adjust retention without code changes.
	•	Flexible retention management across environments.

The scheduler loads this configuration value and uses it to determine which transactions should be deleted.

⸻

5. Scheduler Configuration

The cleanup job is executed through a scheduled task.

Cron Expression

The cron expression is configured in the configuration project and loaded by the application.

0 0 0 ? * SAT

Execution Schedule

Field	Value	Meaning
Second	0	Start at second 0
Minute	0	Minute 0
Hour	0	Midnight
Day of Week	Saturday	Every Saturday

Execution Frequency

Every Saturday at 00:00 (midnight)

Running the cleanup weekly ensures:
	•	Minimal impact on system performance.
	•	Regular cleanup of old transactions.
	•	Reduced database growth.

⸻

6. Cleanup Query

When the scheduler runs, it executes the following cleanup query:

DELETE FROM STOR_INGEST_TXN
WHERE STATUS = 'SUCCESS'
AND STATE = 'COMPLETE'
AND LAST_UPDATE_DTTM < SYSTIMESTAMP - :retentionDays;

Query Explanation

Condition	Purpose
STATUS = 'SUCCESS'	Ensures transaction completed successfully
STATE = 'COMPLETE'	Ensures file cleanup from NAS finished
LAST_UPDATE_DTTM < SYSTIMESTAMP - retentionDays	Ensures retention period has expired

Only transactions meeting all three conditions are removed.

⸻

7. Example Scenario

Assume:

Retention Period = 15 days

Example transaction timeline:

Date	Event
March 1	Transaction created
March 5	Retry
March 10	Retry
March 15	Success + Complete

Retention countdown begins from:

LAST_UPDATE_DTTM = March 15

Transaction becomes eligible for deletion on:

March 30

When the scheduler runs after this date, the transaction will be removed.

⸻

8. Benefits of This Approach

This retention strategy provides several operational benefits:

Safe Cleanup

Transactions are only deleted after successful completion and cleanup.

Accurate Retention Timing

Retention is based on actual completion time, not insertion time.

Configurable Retention

Retention period can be modified through the configuration table without redeployment.

Reduced Database Growth

Completed transactions are periodically removed to maintain database performance.

Operational Stability

Weekly cleanup ensures minimal load on the database while maintaining efficient storage management.

⸻

9. Summary

The transaction retention cleanup process works as follows:
	1.	A scheduler runs every Saturday at midnight.
	2.	The scheduler loads the retention period from the configuration table.
	3.	The system identifies transactions that:
	•	have STATUS = SUCCESS
	•	have STATE = COMPLETE
	•	have LAST_UPDATE_DTTM older than the configured retention period.
	4.	Those transactions are deleted from the STOR_INGEST_TXN table.

This mechanism ensures the transaction table remains optimized while preserving data for the required retention window.

⸻

If you want, I can also generate a small architecture diagram (scheduler → config → cleanup service → DB) which would make this documentation look much better in Confluence.