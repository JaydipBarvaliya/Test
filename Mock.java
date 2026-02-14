@Modifying
@Transactional
@Query("""
    UPDATE StorageIngestTransaction t
       SET t.status = :status,
           t.state = :state,
           t.lastUpdateDttm = :lastUpdateDttm
     WHERE t.ingestTxnId = :ingestTxnId
""")
void updateStatusAndState(
        @Param("ingestTxnId") String ingestTxnId,
        @Param("status") TxnStatus status,
        @Param("state") TxnState state,
        @Param("lastUpdateDttm") OffsetDateTime lastUpdateDttm
);








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

@Service
@RequiredArgsConstructor
public class RetryClaimService {

    private final StorTxnRepository repo;

    @Transactional
    public List<String> claimErrorTransactions(int maxRetries, int batchSize) {

        // Step 1: lock rows
        List<String> ids = repo.lockErrorTransactions(maxRetries, batchSize);

        if (!ids.isEmpty()) {
            // Step 2: move ERROR -> ACTIVE
            repo.markActive(ids);
        }

        // Transaction commits here
        return ids;
    }
}



@Slf4j
@Component
@RequiredArgsConstructor
public class BatchDocRetryScheduler {

    private final RetryClaimService claimService;

    private static final int MAX_RETRIES = 5;
    private static final int BATCH_SIZE = 50;

    @Scheduled(cron = "0 */1 * * * *") // every minute
    public void retryScheduler() {

        List<String> claimedIds =
                claimService.claimErrorTransactions(MAX_RETRIES, BATCH_SIZE);

        if (claimedIds.isEmpty()) {
            log.debug("No ERROR transactions found.");
            return;
        }

        log.info("Claimed {} transactions: {}", claimedIds.size(), claimedIds);
    }
}




INSERT INTO stor_txn
(ingest_txn_id, txn_status, txn_state, retry_count, batch_id, last_update_dttm)
VALUES
('TXN_1', 'ERROR', 'RECEIVED', 0, NULL, SYSTIMESTAMP);

INSERT INTO stor_txn
(ingest_txn_id, txn_status, txn_state, retry_count, batch_id, last_update_dttm)
VALUES
('TXN_2', 'ERROR', 'RECEIVED', 1, NULL, SYSTIMESTAMP);

COMMIT;



SELECT ingest_txn_id, txn_status
FROM stor_txn
WHERE ingest_txn_id IN ('TXN_1','TXN_2');
















