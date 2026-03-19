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