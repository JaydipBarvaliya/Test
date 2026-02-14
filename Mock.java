@Repository
public interface StorTxnRepository extends JpaRepository<StorTxn, String> {

    @Query(value = """
        SELECT ingest_txn_id
        FROM stor_txn
        WHERE txn_status = 'ERROR'
          AND retry_count < :maxRetries
          AND txn_state = 'RECEIVED'
          AND batch_id IS NULL
        FETCH FIRST :batchSize ROWS ONLY
        FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true)
    List<String> lockErrorTransactions(
            @Param("maxRetries") int maxRetries,
            @Param("batchSize") int batchSize
    );

    @Modifying
    @Query(value = """
        UPDATE stor_txn
        SET txn_status = 'ACTIVE',
            last_update_dttm = SYSTIMESTAMP
        WHERE ingest_txn_id IN (:ids)
          AND txn_status = 'ERROR'
        """,
        nativeQuery = true)
    int markActive(@Param("ids") List<String> ids);
}