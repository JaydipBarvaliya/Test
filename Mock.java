/* =======================================================
   REBUILD STOR_CONFIG WITH NEW COLUMN ORDER
   DEV ENVIRONMENT ONLY
   ======================================================= */

-- 1️⃣ Drop Foreign Key from child table
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE STOR_INGEST_TXN DROP CONSTRAINT FK_TXN_CONFIG';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -2443 THEN -- constraint does not exist
            RAISE;
        END IF;
END;
/

-- 2️⃣ Drop STOR_CONFIG table
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE STOR_CONFIG CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN -- table does not exist
            RAISE;
        END IF;
END;
/

-- 3️⃣ Recreate STOR_CONFIG with correct column order
CREATE TABLE STOR_CONFIG (
    ID            NUMBER              NOT NULL,
    LOB_ID        VARCHAR2(50 BYTE)   NOT NULL,
    STOR_SYS      VARCHAR2(50 BYTE)   NOT NULL,
    REPO_ID       VARCHAR2(50 BYTE)   NOT NULL,
    FOLDER_PATH   VARCHAR2(50 BYTE)   NOT NULL,
    NAS_HOST      VARCHAR2(50 BYTE)   NOT NULL,
    NAS_USER      VARCHAR2(50 BYTE)   NOT NULL,
    NAS_PASS      VARCHAR2(50 BYTE)   NOT NULL
);

-- 4️⃣ Recreate Primary Key
ALTER TABLE STOR_CONFIG
ADD CONSTRAINT PK_STOR_CONFIG
PRIMARY KEY (ID);

-- 5️⃣ Recreate Unique Business Constraint
ALTER TABLE STOR_CONFIG
ADD CONSTRAINT UK_STOR_CONFIG_BUSINESS
UNIQUE (LOB_ID, STOR_SYS, REPO_ID);

-- 6️⃣ Recreate Foreign Key from STOR_INGEST_TXN
ALTER TABLE STOR_INGEST_TXN
ADD CONSTRAINT FK_TXN_CONFIG
FOREIGN KEY (STOR_CONFIG_ID)
REFERENCES STOR_CONFIG (ID);

-- 7️⃣ Verify column order
SELECT column_name, column_id
FROM user_tab_columns
WHERE table_name = 'STOR_CONFIG'
ORDER BY column_id;



SELECT constraint_name
FROM user_constraints
WHERE table_name = 'STOR_INGEST_TXN'
AND constraint_type = 'R';
