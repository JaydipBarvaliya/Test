Perfect — I can help you write two clean, Confluence-ready documentation notes for both tables:
	1.	ESIGN_API_DEV.CONFIGURATION
	2.	ESIGN_API_DEV.CLIENT_APP_CONFIGURATION

Each one will clearly explain:
	•	Background and problem
	•	Root cause
	•	Solution implemented (with SQL)
	•	Purpose of the constraint
	•	Benefits and impact
	•	Next steps / validation

⸻

🧩 Documentation 1: Unique Constraint on ESIGN_API_DEV.CONFIGURATION

Background
The CONFIGURATION table stores configuration properties for various applications (APP_ID), lines of business (LOB_ID), and property names (PROP_NAME). During recent validations, duplicate configuration records were found for the same (APP_ID, LOB_ID, PROP_NAME) combination.

Problem Statement
Multiple records with the same combination of APP_ID, LOB_ID, and PROP_NAME were inserted into the table.
These duplicates can cause:
	•	Conflicting configuration values
	•	Unpredictable API behavior when reading configurations
	•	Potential data integrity and performance issues

Root Cause
There was no database-level uniqueness constraint enforcing the combination of APP_ID, LOB_ID, and PROP_NAME.
Even though application logic might attempt to prevent duplicates, direct SQL inserts or parallel processes could still create multiple entries.

Solution
A unique constraint has been added to enforce uniqueness at the database level.

ALTER TABLE ESIGN_API_DEV.CONFIGURATION
ADD CONSTRAINT CONFIG_UNIQUE_APP_LOB_PROP
UNIQUE (APP_ID, LOB_ID, PROP_NAME);

Purpose
	•	To ensure no two configuration records have the same (APP_ID, LOB_ID, PROP_NAME) combination.
	•	To maintain consistent and reliable configuration data.
	•	To delegate data integrity control to the database layer.

Validation Query
To check for duplicates before applying the constraint:

SELECT APP_ID, LOB_ID, PROP_NAME, COUNT(*) AS DUP_COUNT
FROM ESIGN_API_DEV.CONFIGURATION
GROUP BY APP_ID, LOB_ID, PROP_NAME
HAVING COUNT(*) > 1;

To review the detailed duplicate records:

SELECT * 
FROM ESIGN_API_DEV.CONFIGURATION c
WHERE (c.APP_ID, c.LOB_ID, c.PROP_NAME) IN (
    SELECT APP_ID, LOB_ID, PROP_NAME
    FROM ESIGN_API_DEV.CONFIGURATION
    GROUP BY APP_ID, LOB_ID, PROP_NAME
    HAVING COUNT(*) > 1
)
ORDER BY c.APP_ID, c.LOB_ID, c.PROP_NAME, c.ID;

Outcome / Benefits
	•	Enforces strict data integrity.
	•	Eliminates potential configuration conflicts.
	•	Simplifies troubleshooting and downstream logic.
	•	Prevents accidental duplicate inserts from both the API and SQL level.

Next Steps
	•	Clean up existing duplicates before constraint creation.
	•	Validate inserts post-deployment to confirm expected behavior.

⸻

🧩 Documentation 2: Unique Constraint on ESIGN_API_DEV.CLIENT_APP_CONFIGURATION

Background
The CLIENT_APP_CONFIGURATION table maintains configuration mappings between clients (CLIENT_ID) and their respective lines of business (LOB_ID).

Problem Statement
Duplicate combinations of (CLIENT_ID, LOB_ID) were found, allowing the same client and LOB pair to exist multiple times in the system.
This leads to:
	•	Data redundancy
	•	Confusing or conflicting configurations
	•	Issues in downstream joins or reporting

Root Cause
The table lacked a unique key constraint to prevent repeated (CLIENT_ID, LOB_ID) pairs.

Solution
A new unique constraint has been added to enforce the one-to-one relationship between CLIENT_ID and LOB_ID.

ALTER TABLE ESIGN_API_DEV.CLIENT_APP_CONFIGURATION
ADD CONSTRAINT APP_CONFIG_UNIQUE_CLIENT_LOB
UNIQUE (CLIENT_ID, LOB_ID);

Purpose
	•	Prevent duplicate mappings of the same client and LOB combination.
	•	Guarantee accurate configuration references per client.
	•	Improve data consistency across the application.

Validation Query
To check for existing duplicates:

SELECT CLIENT_ID, LOB_ID, COUNT(*) AS DUP_COUNT
FROM ESIGN_API_DEV.CLIENT_APP_CONFIGURATION
GROUP BY CLIENT_ID, LOB_ID
HAVING COUNT(*) > 1;

To review the duplicate records in detail:

SELECT * 
FROM ESIGN_API_DEV.CLIENT_APP_CONFIGURATION c
WHERE (c.CLIENT_ID, c.LOB_ID) IN (
    SELECT CLIENT_ID, LOB_ID
    FROM ESIGN_API_DEV.CLIENT_APP_CONFIGURATION
    GROUP BY CLIENT_ID, LOB_ID
    HAVING COUNT(*) > 1
)
ORDER BY c.CLIENT_ID, c.LOB_ID, c.ID;

Outcome / Benefits
	•	Enforces strict one-to-one mapping between CLIENT_ID and LOB_ID.
	•	Prevents potential data inconsistencies.
	•	Improves data quality for both APIs and reports.
	•	Reduces manual data cleanup effort in the future.

Next Steps
	•	Identify and remove any existing duplicates.
	•	Apply the constraint in all non-prod and prod environments sequentially.
	•	Validate after deployment using the duplicate check queries.

⸻

Would you like me to make both documents Confluence-ready (with proper headings, code formatting, and emojis for clarity like ✅📘💡), or keep it in plain professional Markdown format suitable for internal Jira comments?