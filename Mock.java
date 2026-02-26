if (optionalTxn.isEmpty()) {
    log.debug("Transaction {} no longer exists or already processed", txnId);
    return;
}