private StorConfig resolveStorConfig(String lobId, IngestRequest ingestRequest) {

    List<StorConfig> storConfigs = storConfigRepo.findByLobId(lobId);

    if (storConfigs == null || storConfigs.isEmpty()) {
        log.warn("Invalid LOB ID received: {}", lobId);
        return null;
    }

    if (storConfigs.size() == 1) {
        return storConfigs.get(0);
    }

    // Multiple configs found → resolve by storageSystem + repoId
    String storageSystem = ingestRequest.getStorage().getStorageSystem();
    String repoId = ingestRequest.getStorage().getRepoId();

    Optional<StorConfig> storageConfig =
            storConfigRepo.findByLobIdAndStorageSystemAndRepoId(
                    lobId,
                    storageSystem,
                    repoId
            );

    if (storageConfig.isEmpty()) {
        log.error("No StoreConfig found for lobId={}, storageSystem={}, repoId={}",
                lobId, storageSystem, repoId);
        return null;
    }

    return storageConfig.get();
}


@Override
public ResponseEntity<Ingest200Response> ingest(String lobId,
                                                String traceabilityID,
                                                IngestRequest ingestRequest) {

    log.info("IngestApiDelegateImpl.ingest()");

    String primaryToken = clientFieldValidatorUtil
            .validateClientAppAndExtractPrimaryToken(getRequest(), lobId);

    StorConfig selectedConfig = resolveStorConfig(lobId, ingestRequest);

    if (selectedConfig == null) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // continue your existing logic here...
}



