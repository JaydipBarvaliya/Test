Perfect — let’s make this dead simple and runnable in one go 👍

You want:
	•	1 transaction in STOR_INGEST_TXN
	•	3 chunks in STOR_INGEST_FILE_TRACKING

⸻

✅ One script (copy-paste and run)

-- =========================================
-- 1. INSERT INTO STOR_INGEST_TXN
-- =========================================
INSERT INTO STOR_INGEST_TXN (
    INGEST_TXN_ID,
    LOB_ID,
    TRACEABILITY_ID,
    DGVL_DRAWER_ID,
    DGVL_FOLDER_ID,
    DGVL_FILE_TOKEN,
    DGVL_FILE_NAME,
    NAS_PATH,
    STOR_TXN_ID,
    STOR_FILE_ID,
    STOR_FILE_NAME,
    STOR_CONFIG_ID,
    STATUS,
    STATE,
    RETRY_COUNT,
    CREATION_DTTM,
    LAST_UPDATE_DTTM
) VALUES (
    'TXN_TEST_1',
    'LOB1',
    'TRACE123',
    'DRAWER1',
    'FOLDER1',
    'TOKEN123',
    'file1.pdf',
    '/nas/path/file1.pdf',
    'STOR_TXN_1',
    'FILE_1',
    'file1.pdf',
    1,
    'SUCCESS',
    'COMPLETE',
    0,
    SYSTIMESTAMP - 20,
    SYSTIMESTAMP - 20
);

-- =========================================
-- 2. INSERT 3 CHUNKS INTO FILE TRACKING
-- =========================================

-- Chunk 1
INSERT INTO STOR_INGEST_FILE_TRACKING (
    INGEST_TXN_ID,
    BLOCK_NUM,
    STATUS,
    FILE_BLOCK,
    HASH
) VALUES (
    'TXN_TEST_1',
    1,
    'SUCCESS',
    EMPTY_BLOB(),
    'HASH_1'
);

-- Chunk 2
INSERT INTO STOR_INGEST_FILE_TRACKING (
    INGEST_TXN_ID,
    BLOCK_NUM,
    STATUS,
    FILE_BLOCK,
    HASH
) VALUES (
    'TXN_TEST_1',
    2,
    'SUCCESS',
    EMPTY_BLOB(),
    'HASH_2'
);

-- Chunk 3 (simulate failure case if you want)
INSERT INTO STOR_INGEST_FILE_TRACKING (
    INGEST_TXN_ID,
    BLOCK_NUM,
    STATUS,
    FILE_BLOCK,
    HASH
) VALUES (
    'TXN_TEST_1',
    3,
    'FAILED',
    EMPTY_BLOB(),
    'HASH_3'
);

COMMIT;


⸻

✅ What this gives you

Transaction table

TXN_TEST_1 → SUCCESS / COMPLETE → old (20 days)

Tracking table

TXN_TEST_1 | 1 | SUCCESS
TXN_TEST_1 | 2 | SUCCESS
TXN_TEST_1 | 3 | FAILED


⸻

✅ Now test your cleanup

Run your scheduler/service manually:

cleanupService.cleanupExpiredTransactions(10);


⸻

✅ Verify results

-- Should return 0 rows
SELECT * FROM STOR_INGEST_TXN WHERE INGEST_TXN_ID = 'TXN_TEST_1';

-- Should ALSO return 0 rows (cascade working)
SELECT * FROM STOR_INGEST_FILE_TRACKING WHERE INGEST_TXN_ID = 'TXN_TEST_1';


⸻

🚨 If child rows are NOT deleted

Then your FK is missing:

ON DELETE CASCADE

That’s the only failure point.

⸻

💡 Optional (quick debug query)

SELECT COUNT(*) FROM STOR_INGEST_FILE_TRACKING WHERE INGEST_TXN_ID = 'TXN_TEST_1';

Run before & after cleanup to confirm behavior.

⸻

🔥 Pro tip

If you want to test resume logic later, change:

'FAILED'

for block 3 and simulate retry logic.

⸻

If you want next, I can give you:

👉 SQL to simulate partial progress + resume scenario
👉 OR Java service code to resume from last successful chunk

That’s the next step for your design.