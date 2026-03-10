@Modifying
@Transactional
@Query(value = """
DELETE FROM stor_ingest_txn
WHERE status = 'SUCCESS'
AND state = 'COMPLETE'
AND last_update_dttm < SYSTIMESTAMP - :retentionDays
""", nativeQuery = true)
int deleteExpiredTransactions(@Param("retentionDays") int retentionDays);