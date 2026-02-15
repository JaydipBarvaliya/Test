-- =====================================================
-- STEP 1: Add CONFIG_ID column
-- =====================================================
ALTER TABLE STOR_CONFIG 
ADD CONFIG_ID NUMBER;

-- =====================================================
-- STEP 2: Create sequence for CONFIG_ID
-- =====================================================
CREATE SEQUENCE STOR_CONFIG_SEQ 
START WITH 1 
INCREMENT BY 1 
NOCACHE 
NOCYCLE;

-- =====================================================
-- STEP 3: Populate existing rows with sequence values
-- =====================================================
UPDATE STOR_CONFIG
SET CONFIG_ID = STOR_CONFIG_SEQ.NEXTVAL
WHERE CONFIG_ID IS NULL;

COMMIT;

-- =====================================================
-- STEP 4: Make CONFIG_ID NOT NULL
-- =====================================================
ALTER TABLE STOR_CONFIG 
MODIFY CONFIG_ID NOT NULL;

-- =====================================================
-- STEP 5: Drop old composite primary key (if exists)
-- =====================================================
-- Uncomment if composite PK exists
-- ALTER TABLE STOR_CONFIG DROP CONSTRAINT PK_STOR_CONFIG;

-- =====================================================
-- STEP 6: Make CONFIG_ID the new Primary Key
-- =====================================================
ALTER TABLE STOR_CONFIG
ADD CONSTRAINT PK_STOR_CONFIG
PRIMARY KEY (CONFIG_ID);

-- =====================================================
-- STEP 7: Add Business Unique Constraint
-- (Still enforce uniqueness on LOB_ID + STOR_SYS + REPO_ID)
-- =====================================================
ALTER TABLE STOR_CONFIG
ADD CONSTRAINT UK_STOR_CONFIG_BUSINESS
UNIQUE (LOB_ID, STOR_SYS, REPO_ID);

COMMIT;