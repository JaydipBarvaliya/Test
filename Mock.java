@Async
public void triggerAsync(StorTransaction txn,
                         StorConfig storConfig,
                         String traceabilityId) {

    String txnId = txn.getIngestTxnId();
    long startTime = System.currentTimeMillis();

    log.info("Async BatchDoc trigger started for txnId={}", txnId);

    try {

        log.debug("Calling BatchDoc API for txnId={}, traceabilityId={}",
                txnId, traceabilityId);

        batchDocService.triggerBatchDocAPI(txn, storConfig, traceabilityId);

        long duration = System.currentTimeMillis() - startTime;

        log.info("Async BatchDoc trigger completed successfully for txnId={} in {} ms",
                txnId, duration);

    } catch (Exception ex) {

        log.error("Async BatchDoc trigger FAILED for txnId={}. Updating status to ERROR/RECEIVED",
                txnId, ex);

        storTxnRepository.updateStatusAndState(
                txnId,
                TxnStatus.ERROR,
                TxnState.RECEIVED,
                OffsetDateTime.now()
        );

        log.info("Transaction {} marked as ERROR and RECEIVED for retry.", txnId);
    }
}