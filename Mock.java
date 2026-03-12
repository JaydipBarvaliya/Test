@Query(value = """
SELECT ingest_txn_id
FROM stor_ingest_txn
WHERE status = 'ACTIVE'
  AND state = 'FN_BATCH_TRIGGERED'
  AND last_update_dttm < SYSTIMESTAMP - NUMTODSINTERVAL(:timeoutDays, 'DAY')
FOR UPDATE SKIP LOCKED
""", nativeQuery = true)
List<String> lockTimedOutBatchTransactions(@Param("timeoutDays") int timeoutDays);



@Modifying
@Transactional
@Query(value = """
UPDATE stor_ingest_txn
SET status = 'FAILURE',
    state = 'ERROR',
    last_update_dttm = SYSTIMESTAMP
WHERE ingest_txn_id IN (:ids)
""", nativeQuery = true)
int markBatchTimeoutFailure(@Param("ids") List<String> ids);

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchTimeoutService {

    private final StorTxnRepository storTxnRepository;

    @Transactional
    public int markTimedOutBatchTransactions() {

        List<String> stuckIds = storTxnRepository.lockStuckBatchTransactions();

        if (stuckIds.isEmpty()) {
            return 0;
        }

        int updated = storTxnRepository.markBatchTimeoutFailure(stuckIds);

        log.info("Marked {} BatchDoc timed-out transactions as FAILURE", updated);

        return updated;
    }
}




@Component
@RequiredArgsConstructor
@Slf4j
public class BatchTimeoutScheduler {

    private final BatchTimeoutService batchTimeoutService;

    @Scheduled(cron = "${dgvlm.batch.timeout.scheduler.cron}")
    public void handleBatchTimeouts() {

        log.info("Batch timeout scheduler started");

        int updated = batchTimeoutService.markTimedOutBatchTransactions();

        log.info("Batch timeout scheduler completed. Updated {} records", updated);
    }
}


dgvlm.batch.timeout.scheduler.cron=0 0 */4 * * *


