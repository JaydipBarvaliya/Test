You can tell her this 👇

We can add a unique constraint on (APP_ID, LOB_ID, PROP_NAME) in the CONFIGURATION table.
This will ensure that no one — not even through API or SQL — can insert duplicate configurations with the same combination.
It enforces uniqueness directly at the database level, not just in code.