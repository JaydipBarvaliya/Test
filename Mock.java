-- Check FK
SELECT constraint_name, status
FROM user_constraints
WHERE table_name = 'STOR_INGEST_TXN'
AND constraint_type = 'R';