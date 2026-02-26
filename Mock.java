/* =======================================================
   REBUILD STOR_INGEST_TXN WITH NEW COLUMN ORDER
   - TRACEABILITY_ID as 3rd column
   - Recreates PK, FK, and index on FK
   NOTE: No sequence is created because INGEST_TXN_ID is VARCHAR2(36) (UUID-style)
   ======================================================= */

-- 0) Drop FK from any other tables referencing STOR_INGEST_TXN (if any)
--    (You didn't show any; skip unless needed)

-- 1) Drop FK from STOR_INGEST_TXN -> STOR_CONFIG (if it exists)
BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE STOR_INGEST_TXN DROP CONSTRAINT FK_TXN_CONFIG';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -2443 THEN  -- ORA-02443: constraint does not exist
      RAISE;
    END IF;
END;
/
-- 2) Drop index on FK (if you had one explicitly)
BEGIN
  EXECUTE IMMEDIATE 'DROP INDEX IDX_STOR_INGEST_TXN_CFG';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -1418 AND SQLCODE != -942 THEN
      -- -1418 can appear for some invalid drop index attempts depending on tool
      -- -942 table/view does not exist (some environments show it for indexes too)
      NULL;
    END IF;
END;
/
-- Safer index drop (recommended): ignore "does not exist"
BEGIN
  EXECUTE IMMEDIATE 'DROP INDEX IDX_STOR_INGEST_TXN_CFG';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -1418 AND SQLCODE != -942 AND SQLCODE != -14418 AND SQLCODE != -2443 THEN
      NULL;
    END IF;
END;
/
-- If the above feels messy, use the canonical one:
BEGIN
  EXECUTE IMMEDIATE 'DROP INDEX IDX_STOR_INGEST_TXN_CFG';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -1418 AND SQLCODE != -942 THEN NULL; END IF;
END;
/
-- (If your environment throws different code, we can tighten this later.)

-- 3) Drop PK constraint (if exists) so table drop won't choke in some tools
BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE STOR_INGEST_TXN DROP CONSTRAINT PK_STOR_INGEST_TXN';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -2443 THEN
      RAISE;
    END IF;
END;
/
-- 4) Drop the table
BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE STOR_INGEST_TXN CASCADE CONSTRAINTS';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -942 THEN  -- ORA-00942: table does not exist
      RAISE;
    END IF;
END;
/

-- 5) Recreate STOR_INGEST_TXN with desired column order
CREATE TABLE STOR_INGEST_TXN (
  INGEST_TXN_ID      VARCHAR2(36 CHAR)   NOT NULL,
  LOB_ID             VARCHAR2(50 BYTE)   NOT NULL,
  TRACEABILITY_ID    VARCHAR2(50 BYTE)   NOT NULL,

  DGVL_DRAWER_ID     VARCHAR2(50 BYTE)   NOT NULL,
  DGVL_FOLDER_ID     VARCHAR2(50 BYTE)   NOT NULL,
  DGVL_FILE_TOKEN    VARCHAR2(50 BYTE)   NOT NULL,
  DGVL_FILE_NAME     VARCHAR2(50 BYTE)   NULL,

  STOR_FILE_ID       VARCHAR2(50 BYTE)   NOT NULL,
  STOR_TXN_ID        VARCHAR2(50 BYTE)   NULL,

  STATUS             VARCHAR2(50 BYTE)   NOT NULL,
  STATE              VARCHAR2(50 BYTE)   NOT NULL,

  CREATION_DTTM      TIMESTAMP(6)        NOT NULL,
  LAST_UPDATE_DTTM   TIMESTAMP(6)        NOT NULL,

  RETRY_COUNT        NUMBER(2,0)         NOT NULL,

  NAS_FILE_NAME      VARCHAR2(50 BYTE)   NULL,
  STOR_FILE_NAME     VARCHAR2(50 BYTE)   NULL,

  STOR_CONFIG_ID     NUMBER              NOT NULL
);

-- 6) Recreate Primary Key
ALTER TABLE STOR_INGEST_TXN
  ADD CONSTRAINT PK_STOR_INGEST_TXN
  PRIMARY KEY (INGEST_TXN_ID);

-- 7) Recreate Foreign Key to STOR_CONFIG
-- If you want strict validation (clean data), keep as-is.
-- If you might have bad historical rows, use NOVALIDATE.
ALTER TABLE STOR_INGEST_TXN
  ADD CONSTRAINT FK_TXN_CONFIG
  FOREIGN KEY (STOR_CONFIG_ID)
  REFERENCES STOR_CONFIG (ID);

-- 8) Create index on FK column (recommended for performance)
CREATE INDEX IDX_STOR_INGEST_TXN_CFG
  ON STOR_INGEST_TXN (STOR_CONFIG_ID);

-- 9) Verify column order
SELECT column_name, column_id, nullable, data_type
FROM user_tab_columns
WHERE table_name = 'STOR_INGEST_TXN'
ORDER BY column_id;

-- 10) Verify constraints
SELECT constraint_name, constraint_type, status
FROM user_constraints
WHERE table_name = 'STOR_INGEST_TXN'
ORDER BY constraint_type, constraint_name;

-- 11) Verify indexes
SELECT index_name, uniqueness
FROM user_indexes
WHERE table_name = 'STOR_INGEST_TXN'
ORDER BY index_name;