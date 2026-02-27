@Scheduled(cron = "*/5 * * * * *")
public void retryScheduler() {

    long startTime = System.currentTimeMillis();

    log.info("Retry scheduler started. Batch size: {}", batchSize);

    List<String> claimedIds = claimService.claimErrorTransactions(batchSize);

    if (claimedIds.isEmpty()) {
        log.debug("No ERROR transactions available in this cycle.");
        return;
    }

    log.info("Claimed {} transactions for retry.", claimedIds.size());
    log.debug("Claimed transaction IDs: {}", claimedIds);

    for (String txnId : claimedIds) {
        try {
            log.debug("Processing retry for txnId={}", txnId);
            retryService.process(txnId);
            log.debug("Retry processing completed for txnId={}", txnId);
        } catch (Exception ex) {
            log.error("Scheduler-level failure while processing txnId={}", txnId, ex);
        }
    }

    long duration = System.currentTimeMillis() - startTime;
    log.info("Retry scheduler completed. Processed {} transactions in {} ms.",
            claimedIds.size(), duration);
}