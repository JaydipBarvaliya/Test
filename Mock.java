@Query(value = """
    SELECT ingest_txn_id
    FROM stor_ingest_txn
    WHERE status = 'ERROR'
      AND state = 'RECEIVED'
    FOR UPDATE SKIP LOCKED
    """,
    nativeQuery = true)
List<String> lockErrorTransactions();



@Transactional
public List<String> getBatch(int batchSize) {
    return repo.lockErrorTransactions()
               .stream()
               .limit(batchSize)
               .toList();
}


