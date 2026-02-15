@Query(value = """
    SELECT ingest_txn_id
    FROM (
        SELECT ingest_txn_id
        FROM stor_ingest_txn
        WHERE status = 'ERROR'
          AND state = 'RECEIVED'
        FOR UPDATE SKIP LOCKED
    )
    WHERE ROWNUM <= :batchSize
    """,
    nativeQuery = true)
List<String> lockErrorTransactions(@Param("batchSize") int batchSize);