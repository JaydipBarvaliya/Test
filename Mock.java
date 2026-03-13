@Override
public ResponseEntity<AdminIngestRs> adminIngest(
        String traceabilityID,
        AdminIngestRequest adminIngestRequest) {

    log.info("Admin ingest API invoked with {} txnIds. traceabilityId={}",
            adminIngestRequest.getTxnsToReprocess().size(),
            LogSanitizeUtil.sanitizeLogObj(traceabilityID));

    AdminIngestRs response =
            adminIngestService.reprocessTransactions(adminIngestRequest);

    log.info("Admin ingest completed. requested={}, success={}, notFound={}, traceabilityId={}",
            response.getRequestedTxnCount(),
            response.getSuccessCount(),
            response.getNotFoundCount(),
            LogSanitizeUtil.sanitizeLogObj(traceabilityID));

    return ResponseEntity.ok(response);
}