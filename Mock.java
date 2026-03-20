-- ============================================
-- DROP SEQUENCE (if exists)
-- ============================================
BEGIN
   EXECUTE IMMEDIATE 'DROP SEQUENCE PERF_STATS_SEQ';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -2289 THEN -- sequence does not exist
         RAISE;
      END IF;
END;
/
-- ============================================
-- DROP TABLE (if exists)
-- ============================================
BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE PERF_STATS CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN
      IF SQLCODE != -942 THEN -- table does not exist
         RAISE;
      END IF;
END;
/
-- ============================================
-- CREATE TABLE
-- ============================================
CREATE TABLE PERF_STATS (
    ID              NUMBER(20) PRIMARY KEY,
    INGEST_TXN_ID   VARCHAR2(36),
    BATCH_ID        VARCHAR2(100),
    TRACE_ID        VARCHAR2(100),

    MODULE_NAME     VARCHAR2(100),
    STEP_NAME       VARCHAR2(100),
    TRANS_TYPE      VARCHAR2(100),

    START_TIME      TIMESTAMP,
    END_TIME        TIMESTAMP,
    DURATION_MS     NUMBER,

    STATUS          VARCHAR2(20),

    CREATED_AT      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- ============================================
-- CREATE SEQUENCE
-- ============================================
CREATE SEQUENCE PERF_STATS_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;