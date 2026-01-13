try {
    BatchDocResponse response = webClient.post()
        .uri(batchDocUrl)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(BatchDocResponse.class)
        .block();

    txn.setStorTxnId(response.batchId());
    txn.setStatus("ACTIVE");
    txn.setState("FN_BATCH_TRIGGERED");
    txn.setLastUpdatedTs(OffsetDateTime.now());
    storTxnRepository.save(txn);

} catch (WebClientResponseException ex) {

    log.error("BatchDoc failed, status={}, body={}",
        ex.getStatusCode(), ex.getResponseBodyAsString(), ex);

    storTxnRepository.updateStatusAndState(
        txn.getDgvlmId(),
        "ERROR",
        "DGVM_PUSHED",
        OffsetDateTime.now()
    );

} catch (Exception ex) {

    log.error("Unexpected BatchDoc error for dgvlmId={}", txn.getDgvlmId(), ex);

    storTxnRepository.updateStatusAndState(
        txn.getDgvlmId(),
        "ERROR",
        "DGVM_PUSHED",
        OffsetDateTime.now()
    );
}