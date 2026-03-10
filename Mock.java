@Component
@RequiredArgsConstructor
@Slf4j
public class TxnRetentionCleanupScheduler {

    private final TransactionCleanupService cleanupService;

    @Scheduled(cron = "${txn.cleanup.cron}")
    public void cleanupOldTransactions() {

        log.info("Transaction cleanup scheduler started");

        int deleted = cleanupService.cleanupExpiredTransactions();

        log.info("Transaction cleanup completed. Deleted {} records.", deleted);
    }
}


@Service
@RequiredArgsConstructor
public class TransactionCleanupService {

    private final StorTxnRepository repository;

    @Transactional
    public int cleanupExpiredTransactions() {

        return repository.deleteExpiredTransactions();
    }
}



@Modifying
@Query(value = """
DELETE FROM stor_ingest_txn
WHERE creation_dttm < SYSTIMESTAMP - INTERVAL '15' DAY
""", nativeQuery = true)
int deleteExpiredTransactions();



txn.cleanup.cron=0 0 2 * * ?


DELETE FROM stor_ingest_txn
WHERE creation_dttm < SYSTIMESTAMP - INTERVAL '15' DAY
AND ROWNUM <= 1000


I think I understand the issue now.

Currently the pillar is creating two entries (tdgvlm942nasb and nasuserpass), but the CIFS formula expects the credential name to match the vault secret key.

I’ll update the configuration so that the vault contains a single secret named TDGVLM942NASB, and during the pipeline I will provide the NAS password as its value. This should produce the expected pillar format:
