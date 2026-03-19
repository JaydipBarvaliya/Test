Good, this is exactly the right move 👍 — but don’t just copy ESIGN blindly. That table is API-centric, while your DGVLM is pipeline + transaction + batch-oriented. If you copy it 1:1, you’ll regret it later.

Let’s design this properly like an architect, not like a copier.

⸻

🔥 First — What ESIGN table is doing (core idea)

That table is tracking:
	•	Request metadata (APP_ID, TRACE_ID, CLIENT_ID)
	•	Infra info (JVM, IP)
	•	Timing (PROCESS_TIME, API_RESPONSE_TIME)
	•	Business context (PACKAGE_ID, TRANS_TYPE)
	•	Result (HTTP_STATUS_CD)
	•	Extra debug (DETAILS)

👉 It’s basically API observability table

⸻

⚠️ Your DGVLM is different

Your system is:
	•	Batch-driven
	•	Transaction-driven (STOR_INGEST_TXN)
	•	Async + retry + scheduler heavy
	•	External integrations (NAS, DigiVLA, BatchDoc, etc.)

👉 So you need PROCESS + PIPELINE observability, not just API logs

⸻

💡 Final Recommendation: Split thinking into 3 layers

Don’t shove everything in one table.

1. Transaction Level (already exists)
	•	STOR_INGEST_TXN → business state

2. NEW: DGVLM_PERF_STATS (this one you’re designing)
	•	Each step / API / operation logging

3. (Optional but powerful) Step-level breakdown
	•	If needed later → separate table

⸻

✅ Proposed Table: DGVLM_PERF_STATS

🔷 Core Idea

Each row = one operation / step execution

⸻

🧠 Schema Design (Clean + Scalable)

CREATE TABLE DGVLM_PERF_STATS (
    
    ID                NUMBER(20) PRIMARY KEY,
    
    -- 🔗 Correlation
    INGEST_TXN_ID     NUMBER(20),         -- FK to STOR_INGEST_TXN
    BATCH_ID          VARCHAR2(100),      -- Optional (if batch driven)
    TRACE_ID          VARCHAR2(100),      -- For request tracing
    
    -- 📌 Context
    MODULE_NAME       VARCHAR2(100),      -- e.g. INGEST_SERVICE, RETRY_SCHEDULER
    STEP_NAME         VARCHAR2(100),      -- e.g. UPLOAD_TO_NAS, CALL_DIGIVLA
    TRANS_TYPE        VARCHAR2(100),      -- CREATE, RETRY, DELETE
    
    -- 🌐 System Info
    APP_NAME          VARCHAR2(100),
    HOST_NAME         VARCHAR2(200),
    IP_ADDRESS        VARCHAR2(100),
    
    -- ⏱ Timing
    START_TIME        TIMESTAMP(6),
    END_TIME          TIMESTAMP(6),
    PROCESS_TIME_MS   NUMBER(10),
    
    -- 🔗 External Calls
    EXTERNAL_SYSTEM   VARCHAR2(100),      -- DIGIVLA / BATCHDOC / NAS
    API_NAME          VARCHAR2(200),      -- endpoint name
    API_RESPONSE_TIME VARCHAR2(500),      -- same format like ESIGN (optional multi)
    
    -- 📊 Result
    STATUS            VARCHAR2(50),       -- SUCCESS / FAILED / RETRY
    ERROR_CODE        VARCHAR2(100),
    ERROR_MESSAGE     VARCHAR2(1000),
    HTTP_STATUS_CODE  VARCHAR2(50),
    
    -- 📦 Payload Metadata (VERY USEFUL)
    FILE_COUNT        NUMBER(5),
    TOTAL_SIZE_KB     NUMBER(10),
    
    -- 🧾 Debug
    DETAILS           VARCHAR2(4000),
    
    CREATED_AT        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


⸻

🔥 Why this is better than ESIGN

✅ You removed irrelevant stuff
	•	❌ PDF_TRANSFORM
	•	❌ NBR_SIGNERS
	•	❌ NBR_DOCS

Good call 👍

⸻

✅ You added what actually matters

1. STEP_NAME 🔥
This is HUGE.

Instead of generic logs:

TRANS_TYPE = CREATE

You now know:

STEP_NAME = UPLOAD_TO_NAS
STEP_NAME = CALL_DIGIVLA
STEP_NAME = UPDATE_DB

👉 This gives real debugging power

⸻

2. INGEST_TXN_ID (must-have)
This ties everything back to:

STOR_INGEST_TXN

👉 Without this, your table is useless

⸻

3. MODULE_NAME
Helps identify:
	•	Scheduler
	•	API
	•	Retry worker

⸻

4. STATUS (don’t rely only on HTTP code)
Because:
	•	Some failures are internal
	•	Some are retries

⸻

5. EXTERNAL_SYSTEM
You can easily filter:

WHERE EXTERNAL_SYSTEM = 'DIGIVLA'


⸻

6. PROCESS_TIME_MS
Don’t compute later → store it

⸻

💥 Optional (Advanced — if you want to be 10/10)

Add Indexes

CREATE INDEX IDX_DGVLM_TXN ON DGVLM_PERF_STATS (INGEST_TXN_ID);
CREATE INDEX IDX_DGVLM_TRACE ON DGVLM_PERF_STATS (TRACE_ID);
CREATE INDEX IDX_DGVLM_STATUS ON DGVLM_PERF_STATS (STATUS);


⸻

Add Partition (if high volume)

PARTITION BY RANGE (CREATED_AT)


⸻

⚠️ Brutally honest feedback

👉 If you just copy ESIGN table, you will:
	•	Lose step-level visibility ❌
	•	Struggle debugging retries ❌
	•	Mix API + batch logic ❌

👉 If you use this design:
	•	You get end-to-end traceability ✅
	•	You can build dashboards later ✅
	•	You can debug failures in minutes ✅

⸻

💬 Final call (what I’d do in your place)

I’d go with:
	•	INGEST_TXN_ID as primary linkage
	•	STEP_NAME + MODULE_NAME combo
	•	Keep table lean but extensible
	•	Don’t over-normalize yet

⸻

If you want next level, I can help you:

👉 Write Spring Boot interceptor / AOP logger to auto-insert this
👉 Or design retry tracking + perf stats together
👉 Or create Confluence-ready doc like ESIGN one

Just say 👍









Good — this is already 80% solid 👍
Now what you’re missing is real, meaningful comments + examples so people don’t misuse the table later.

I’ll tighten this like production-grade documentation.

⸻

🔥 Updated Column Comments + Examples (Use this in Confluence)

🔹 Identity & Correlation

Column	Comment	Example
ID	Unique identifier for each performance record	162345
INGEST_TXN_ID	Foreign key reference to STOR_INGEST_TXN	10024567
BATCH_ID	Batch identifier for grouped processing (if applicable)	BATCH_20260318_001
TRACE_ID	Unique trace identifier for end-to-end request tracking across systems	42e03e50-88f6-11f0-9af8-aa0d9ae44bb5


⸻

🔹 Execution Context

Column	Comment	Example
MODULE_NAME	Logical component executing the step	INGEST_SERVICE, RETRY_SCHEDULER, CLEANUP_JOB
STEP_NAME	Specific operation being executed	UPLOAD_TO_NAS, CALL_DGVLA, UPDATE_STATUS_DB
TRANS_TYPE	Type of business operation	CREATE, RETRY, DELETE, REPROCESS

👉 Important:
	•	MODULE_NAME = who is executing
	•	STEP_NAME = what exactly is happening

⸻

🔹 System Info (Infra Debugging)

Column	Comment	Example
APP_NAME	Application name generating the record	DGVLM, DGVLA
HOST_NAME	Server/VM where process executed	craesigsbdzai10.dev.vmc2.td.com
IP_ADDRESS	IP address of the executing node	10.44.222.176


⸻

🔹 Timing Metrics

Column	Comment	Example
START_TIME	Timestamp when processing started	18-MAR-26 10.15.30.123000
END_TIME	Timestamp when processing completed	18-MAR-26 10.15.32.456000
PROCESS_TIME_MS	Total execution time in milliseconds	2333

👉 Always store PROCESS_TIME_MS
Don’t calculate later — you’ll lose precision.

⸻

🔹 External Interaction

Column	Comment	Example
EXTERNAL_SYSTEM	External system invoked	DIGIVLA, BATCHDOC, NAS
API_NAME	API endpoint or operation name	/uploadDocument, /createBatch
API_RESPONSE_TIME	Response times of external APIs (semicolon-separated if multiple)	DV:364; FF_API:1306; NAS:598

👉 Follow ESIGN pattern here — it’s actually useful.

⸻

🔹 Execution Result

Column	Comment	Example
STATUS	Final execution status	SUCCESS, FAILED, RETRY, PARTIAL_SUCCESS
ERROR_CODE	System or business error code	DGVLM_5001, NAS_TIMEOUT
ERROR_MESSAGE	Detailed error message (truncated if needed)	Failed to upload file to NAS due to timeout
HTTP_STATUS_CODE	HTTP response code from external API	200, 500, 404

👉 Important rule:
	•	STATUS ≠ HTTP status
	•	Internal failures can still have HTTP 200

⸻

🔹 Payload Metadata

Column	Comment	Example
FILE_COUNT	Number of files processed in this step	5

👉 Optional (if you want to extend later):
	•	TOTAL_SIZE_KB
	•	RETRY_COUNT

⸻

🔥 Extra You Should Add (Small but powerful)

You missed one thing 👇

✅ Add this column

RETRY_COUNT NUMBER(3)

Why?
	•	You are doing retries
	•	You’ll want analytics like:

How many retries before success?


⸻

🔥 Example Row (This is GOLD for docs)

Put this in Confluence 👇

ID: 1001
INGEST_TXN_ID: 50021
BATCH_ID: BATCH_20260318_01
TRACE_ID: 42e03e50-88f6-11f0

MODULE_NAME: INGEST_SERVICE
STEP_NAME: UPLOAD_TO_NAS
TRANS_TYPE: CREATE

APP_NAME: DGVLM
HOST_NAME: craesigsbdzai10.dev.vmc2.td.com
IP_ADDRESS: 10.44.222.176

START_TIME: 18-MAR-26 10:15:30
END_TIME: 18-MAR-26 10:15:32
PROCESS_TIME_MS: 2100

EXTERNAL_SYSTEM: NAS
API_NAME: /uploadFile
API_RESPONSE_TIME: NAS:2100

STATUS: SUCCESS
ERROR_CODE: null
ERROR_MESSAGE: null
HTTP_STATUS_CODE: 200

FILE_COUNT: 5


⸻

⚠️ Final Brutal Feedback

You’re very close, but watch these:

❌ Don’t make it “logging table”

Make it analytics-ready table

❌ Don’t leave comments vague

Future devs WILL misuse fields

❌ Don’t overload DETAILS

Use structured columns first, then DETAILS

⸻

💯 Final Verdict

✔ Your schema = strong
✔ With these comments = production-ready
✔ With retry_count = analytics-ready

⸻

If you want next level:

👉 I can design dashboard queries (avg time per step, failure rate, retry heatmap)
👉 Or help you auto-populate this using Spring AOP

Just say 👍
