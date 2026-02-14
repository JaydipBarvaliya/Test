@Slf4j
@Component
@RequiredArgsConstructor
public class BatchDocRetryScheduler {

    private final RetryClaimService claimService;
    private final BatchDocRetryService retryService;

    private static final int BATCH_SIZE = 50;

    @Scheduled(cron = "0 */1 * * * *")
    public void retryScheduler() {

        List<String> claimedIds =
                claimService.claimErrorTransactions(BATCH_SIZE);

        if (claimedIds.isEmpty()) {
            log.debug("No ERROR transactions found.");
            return;
        }

        log.info("Claimed {} transactions", claimedIds.size());

        for (String txnId : claimedIds) {
            retryService.process(txnId);
        }
    }
}



@Service
@RequiredArgsConstructor
@Slf4j
public class BatchDocRetryService {

    private final StorTxnRepository repo;
    private final BatchDocService batchDocService; // your existing service

    private static final int MAX_RETRIES = 5;

    @Transactional
    public void process(String txnId) {

        Optional<StorageIngestTransaction> optionalTxn =
                repo.findByIngestTxnId(txnId);

        if (optionalTxn.isEmpty()) {
            log.warn("Transaction not found: {}", txnId);
            return;
        }

        StorageIngestTransaction txn = optionalTxn.get();

        try {
            // 🔥 Call existing logic
            batchDocService.triggerBatchDocAPIAsync(txn);

            // On success:
            txn.setState(TxnState.FN_BATCH_TRIGGERED);
            txn.setStatus(TxnStatus.ACTIVE);
            txn.setLastUpdateDttm(OffsetDateTime.now());

            repo.save(txn);

        } catch (Exception ex) {

            log.error("Retry failed for txnId={}", txnId, ex);

            int newRetryCount = txn.getRetryCount() + 1;
            txn.setRetryCount(newRetryCount);

            if (newRetryCount >= MAX_RETRIES) {
                txn.setStatus(TxnStatus.FAILURE);
            } else {
                txn.setStatus(TxnStatus.ERROR);
            }

            txn.setLastUpdateDttm(OffsetDateTime.now());

            repo.save(txn);
        }
    }
}





