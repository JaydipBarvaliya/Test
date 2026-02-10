@Override
public ResponseEntity<IngestResponse> ingest(
        String lobId,
        String traceabilityId,
        IngestRequest ingestRequest
) {

    clientFieldValidatorUtil.validateClientApp(getRequest(), lobId);

    /*
     * TEMPORARY LOGIC:
     *
     * 1. First fetch configs by LOB ID only.
     * 2. If exactly one config exists, use it.
     * 3. If multiple configs exist, fall back to lookup using
     *    (LOB ID + Storage System + Repo ID).
     *
     * TODO:
     * - This method should NOT rely on result size.
     * - Controller/API contract should explicitly pass
     *   storageSystem and repoId.
     * - Repository should return Optional<StorageConfig>
     *   using (LOB ID + Storage System + Repo ID).
     * - A UNIQUE constraint must exist at DB level for
     *   (LOB ID, Storage System, Repo ID).
     */

    List<StorageConfig> storageConfigs =
            storageConfigRepo.findByLobId(lobId);

    if (storageConfigs == null || storageConfigs.isEmpty()) {
        log.warn("Invalid LOB ID received: {}", lobId);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    StorageConfig selectedConfig;

    if (storageConfigs.size() == 1) {
        selectedConfig = storageConfigs.get(0);
    } else {
        // TEMP placeholders until API contract is finalized
        String storageSystem = "???";
        String repoId = "???";

        List<StorageConfig> refinedConfigs =
                storageConfigRepo.findByLobIdAndStorageSystemAndRepoId(
                        lobId,
                        storageSystem,
                        repoId
                );

        if (refinedConfigs == null || refinedConfigs.size() != 1) {
            log.error(
                "Ambiguous StoreConfig for lobId={}, storageSystem={}, repoId={}",
                lobId, storageSystem, repoId
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        selectedConfig = refinedConfigs.get(0);
    }

    StorageTransaction txn =
            ingestService.ingest(lobId, traceabilityId, ingestRequest, selectedConfig);

    batchDocService.triggerBatchDocAPIAsync(
            txn,
            selectedConfig,
            lobId,
            traceabilityId
    );

    IngestResponse response = new IngestResponse();
    response.setTxnId(txn.getTxnId());

    return ResponseEntity.ok(response);
}