Good. 👍 You’re not far off. You just mixed sequence names and constraints. Let’s clean this properly.

From your screenshot:

ALTER TABLE STOR_CONFIG ADD CONFIG_ID NUMBER;

CREATE SEQUENCE STOR_CONFIG_SEQ START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;

UPDATE STOR_CONFIG SET CONFIG_ID = STOR_CONFIG_SEQ.NEXTVAL;

The ORA-00933 error is likely because:

• You already created the sequence earlier
• Or you executed partially
• Or missing semicolon between statements
• Or ran all together in worksheet incorrectly

Let’s fix this cleanly step-by-step.

⸻

✅ Step 1 — Check What Exists First

Run:

SELECT sequence_name FROM user_sequences WHERE sequence_name = 'STOR_CONFIG_SEQ';

If it exists → do NOT recreate it.

If it does not exist → create it.

⸻

✅ Step 2 — If Sequence Exists, Just Populate IDs

If CONFIG_ID column exists but values are NULL:

UPDATE STOR_CONFIG
SET CONFIG_ID = STOR_CONFIG_SEQ.NEXTVAL
WHERE CONFIG_ID IS NULL;

Commit:

COMMIT;


⸻

✅ Step 3 — Make CONFIG_ID NOT NULL

Only after values are filled:

ALTER TABLE STOR_CONFIG
MODIFY CONFIG_ID NOT NULL;


⸻

✅ Step 4 — Make CONFIG_ID Primary Key

If you still have composite PK, drop it:

ALTER TABLE STOR_CONFIG DROP CONSTRAINT PK_STOR_CONFIG;

Then:

ALTER TABLE STOR_CONFIG
ADD CONSTRAINT PK_STOR_CONFIG PRIMARY KEY (CONFIG_ID);


⸻

✅ Step 5 — Re-add Business Unique Constraint

Very important:

ALTER TABLE STOR_CONFIG
ADD CONSTRAINT UK_STOR_CONFIG_BUSINESS
UNIQUE (LOB_ID, STOR_SYS, REPO_ID);


⸻

⚠ Why You Got ORA-00001 Earlier

From your earlier screenshot:

ORA-00001: unique constraint (PK_STOR_CONFIG) violated

That means:

You already had a primary key on CONFIG_ID and you tried inserting duplicate values manually.

Let the sequence generate it automatically.

Do NOT manually insert CONFIG_ID values unless you know what you’re doing.

⸻

🎯 Clean State You Should Have

STOR_CONFIG table:
	•	CONFIG_ID (PK)
	•	LOB_ID
	•	STOR_SYS
	•	REPO_ID
	•	UNIQUE(LOB_ID, STOR_SYS, REPO_ID)

That’s correct architecture.

⸻

Now next logical step:

Add CONFIG_ID as foreign key to STOR_INGEST_TXN.

If you’re ready, I’ll give you that exact safe migration script.

You’re doing the right thing. Just slow down and execute cleanly. 💪