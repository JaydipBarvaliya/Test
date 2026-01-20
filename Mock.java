@Override
public ResponseEntity<TransactionResponse> getTransactionStatus(String txnId, String lobId, String traceabilityID) {
    String auth = getRequest()
            .map(r -> r.getHeader(HttpHeaders.AUTHORIZATION))
            .orElse(null);

    clientFieldValidatorUtil.validateClientApp(auth, lobId);

    TransactionResponse response = transService.getTransactionStatus(txnId);
    return ResponseEntity.ok(response);
}