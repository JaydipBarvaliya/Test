Optional<StorageConfig> refinedConfig =
        storageConfigRepo.findByLobIdAndStorageSystemAndRepoId(
                lobId,
                storageSystem,
                repoId
        );

if (refinedConfig.isEmpty()) {
    log.error(
        "No StoreConfig found for lobId={}, storageSystem={}, repoId={}",
        lobId, storageSystem, repoId
    );
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
}

selectedConfig = refinedConfig.get();