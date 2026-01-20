@Override
public ResponseEntity<TransactionResponse> getTransactionStatus(String txnId, String lobId, String traceabilityID) {
    String auth = getRequest()
            .map(r -> r.getHeader(HttpHeaders.AUTHORIZATION))
            .orElse(null);

    clientFieldValidatorUtil.validateClientApp(auth, lobId);

    TransactionResponse response = transService.getTransactionStatus(txnId);
    return ResponseEntity.ok(response);
}



public void validateClientApp(String authorizationHeader, String lobId) throws DgvlmServiceException {
    HttpHeaders headers = new HttpHeaders();
    if (authorizationHeader != null && !authorizationHeader.isBlank()) {
        headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
    }
    validateClients(headers, lobId);
}