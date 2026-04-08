-- =====================================================
-- DROP TABLE
-- =====================================================
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE PERF_STATS_API PURGE';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

-- =====================================================
-- CREATE TABLE
-- =====================================================
CREATE TABLE PERF_STATS_API (
    ID                  NUMBER(20,0)        NOT NULL,
    APP_ID              VARCHAR2(100 BYTE),
    LOB_ID              VARCHAR2(100 BYTE),
    TRACE_ID            VARCHAR2(100 BYTE),
    CLIENT_ID           VARCHAR2(100 BYTE),
    INET_ADDR           VARCHAR2(100 BYTE),
    JVM_NAME            VARCHAR2(400 BYTE),
    REQ_DATE            TIMESTAMP,
    TRANS_TYPE          VARCHAR2(200 BYTE),
    PACKAGE_ID          VARCHAR2(64 BYTE),
    HTTP_STATUS_CD      VARCHAR2(500 BYTE),
    PROCESS_TIME        NUMBER(10,0),
    API_RESPONSE_TIME   VARCHAR2(500 BYTE),
    DETAILS             VARCHAR2(4000 BYTE),
    NBR_DOCS            NUMBER(2,0),
    NBR_SIGNERS         NUMBER(3,0),
    PDF_TRANSFORM       VARCHAR2(100 BYTE)
);

-- =====================================================
-- PRIMARY KEY
-- =====================================================
ALTER TABLE PERF_STATS_API
ADD CONSTRAINT PERF_STATS_ID
PRIMARY KEY (ID);

-- =====================================================
-- INDEX (as per seed)
-- =====================================================
CREATE INDEX PERF_STATS_PACKAGE_ID_REQ_DATE
ON PERF_STATS_API (PACKAGE_ID, REQ_DATE);