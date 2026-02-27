@Transactional
public void process(String ingestTxnId) {

    long startTime = System.currentTimeMillis();

    log.debug("Retry processing started for ingestTxnId={}", ingestTxnId);

    Optional<StorTransaction> optionalTxn =
            txnRepo.findByIngestTxnId(ingestTxnId);

    if (optionalTxn.isEmpty()) {
        log.warn("Retry skipped. Transaction {} does not exist or already processed.",
                ingestTxnId);
        return;
    }

    StorTransaction txn = optionalTxn.get();

    try {

        log.debug("Triggering BatchDoc API for ingestTxnId={}, currentRetryCount={}",
                ingestTxnId, txn.getRetryCount());

        batchDocService.triggerBatchDocAPI(
                txn,
                txn.getConfig(),
                txn.getTraceabilityId()
        );

        long duration = System.currentTimeMillis() - startTime;

        log.info("Retry successful for ingestTxnId={} in {} ms",
                ingestTxnId, duration);

    } catch (Exception ex) {

        log.error("Retry failed for ingestTxnId={}", ingestTxnId, ex);

        int newRetryCount = txn.getRetryCount() + 1;
        txn.setRetryCount(newRetryCount);

        if (newRetryCount >= failureRetryMax) {

            txn.setStatus(TxnStatus.FAILURE);

            log.warn("Retry limit reached for ingestTxnId={}. Marked as FAILURE. retryCount={}",
                    ingestTxnId, newRetryCount);

        } else {

            txn.setStatus(TxnStatus.ERROR);

            log.info("Retry scheduled again for ingestTxnId={}. retryCount={}/{}",
                    ingestTxnId, newRetryCount, failureRetryMax);
        }

        txn.setLastUpdateDttm(OffsetDateTime.now());
        txn.setState(TxnState.RECEIVED);

        txnRepo.save(txn);

        log.debug("Transaction {} updated in DB with status={} state={} retryCount={}",
                ingestTxnId,
                txn.getStatus(),
                txn.getState(),
                txn.getRetryCount());
    }
}