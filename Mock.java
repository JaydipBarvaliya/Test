@Modifying
@Transactional
@Query(value = """
DELETE FROM stor_ingest_txn
WHERE creation_dttm < SYSTIMESTAMP - :retentionDays
""", nativeQuery = true)
int deleteExpiredTransactions(@Param("retentionDays") int retentionDays);