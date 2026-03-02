These scripts perform a controlled rebuild of the STOR_CONFIG and STOR_INGEST_TXN tables by safely dropping dependent constraints, recreating the tables with the correct structure, and reapplying all primary, unique, and foreign key constraints.

The process also recreates associated sequences and includes exception handling to ensure idempotent and safe execution across environments.