@Modifying
@Transactional
@Query(value = """
UPDATE stor_ingest_txn
SET status = 'ERROR',
    last_update_dttm = SYSTIMESTAMP
WHERE ingest_txn_id = :txnId
AND status = 'FAILURE'
""", nativeQuery = true)
int updateStatusToError(@Param("txnId") String txnId);