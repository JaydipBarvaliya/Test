@Override
public ResponseEntity<Ingest200Response> ingest(
        String lobId,
        String traceabilityID,
        IngestRequest ingestRequest
) {

    // 1. Validate LOB ID using STOR_CONFIG
    List<StorConfigEntity> storConfigs =
            storConfigRepository.findByIdLobId(lobId);

    if (storConfigs == null || storConfigs.isEmpty()) {
        log.warn("Invalid LOB ID received: {}", lobId);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // 2. Create STOR_TXN record
    UUID dgvlmId = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now();

    StorTxnEntity txn = new StorTxnEntity();
    txn.setDgvlmId(dgvlmId);
    txn.setLobId(lobId);
    txn.setDrawerId(ingestRequest.getDrawerId());
    txn.setFolderId(ingestRequest.getFolderId());
    txn.setFileName(ingestRequest.getFileName());
    txn.setStorFileId(ingestRequest.getStorFileId());
    txn.setStorTxnId(null);
    txn.setStatus("NEW");
    txn.setState("RECEIVED");
    txn.setCreatedTs(now);
    txn.setLastUpdatedTs(now);

    // 3. Persist
    storTxnRepository.save(txn);

    // 4. Build response
    Ingest200Response response = new Ingest200Response();
    response.setDgvlmId(dgvlmId.toString());

    return ResponseEntity.ok(response);
}