Good catch — right now your doc is technically incomplete.
You’re deleting from STOR_INGEST_TXN, but nowhere does it explicitly say what happens to:

👉 STOR_INGEST_FILE_TRACKING

That’s a gap, and reviewers will question it.

⸻

🔥 Where you should update (exact spots)

✅ 1. Overview section (MUST update)

Right now it says:

remove completed transactions from STOR_INGEST_TXN

👉 Fix it to reflect full cleanup

✔ Replace with:

The Transaction Retention Cleanup Process is implemented to automatically remove completed transactions 
from the STOR_INGEST_TXN table along with all associated records in the STOR_INGEST_FILE_TRACKING table 
after a configured retention period.


⸻

✅ 2. Transaction Completion Criteria (add 1 bullet)

After this line:

Associated file has been successfully removed from NAS

👉 Add this:

• All file chunk tracking records (STOR_INGEST_FILE_TRACKING) are no longer required and will be deleted as part of cleanup.


⸻

✅ 3. Cleanup Query section (MOST IMPORTANT)

Right now you only show:

DELETE FROM stor_ingest_txn

That’s misleading.

⸻

🔥 Add this explanation BELOW query:

Note:
Deletion of records from STOR_INGEST_TXN automatically removes all corresponding records 
from STOR_INGEST_FILE_TRACKING via foreign key constraint with ON DELETE CASCADE.


⸻

🚨 If NOT using cascade (be honest here)

Then you must show:

DELETE FROM stor_ingest_file_tracking
WHERE ingest_txn_id IN (
    SELECT ingest_txn_id
    FROM stor_ingest_txn
    WHERE status = 'SUCCESS'
    AND state = 'COMPLETE'
    AND last_update_dttm < SYSTIMESTAMP - :retentionDays
);

DELETE FROM stor_ingest_txn ...


⸻

✅ 4. Summary section (add 1 line)

Right now it ends with:

deleted from STOR_INGEST_TXN table

👉 Fix it:

4. Those transactions and all associated file tracking records are deleted 
   from STOR_INGEST_TXN and STOR_INGEST_FILE_TRACKING tables.


⸻

💡 What NOT to do (important)

❌ Don’t mention FetchType.LAZY — irrelevant here
❌ Don’t talk about JPA cascade — this is DB-level cleanup
❌ Don’t overcomplicate with chunk logic here

👉 This doc is scheduler + retention, keep it DB-focused.

⸻

⚠️ Brutal truth (important)

Right now your system assumes:

👉 Either:
	•	FK has ON DELETE CASCADE ✅ (best)

OR
	•	You manually delete child records ❌ (error-prone)

If neither is true → you have a data leak bug

⸻

💬 Final recommendation (do this)

If you haven’t already, confirm this exists:

FOREIGN KEY (INGEST_TXN_ID)
REFERENCES STOR_INGEST_TXN(INGEST_TXN_ID)
ON DELETE CASCADE


⸻

🚀 If you want next step

I can:
	•	Audit your DDL and fix FK properly
	•	Or rewrite this whole Confluence page into production-grade doc (review-proof)

Just say 👍