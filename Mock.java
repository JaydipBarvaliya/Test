private StorConfig getStorConfig(String lobId, IngestRequest ingestRequest)
        throws DgvLmServiceException {

    List<StorConfig> configs = Optional
            .ofNullable(storConfigRepo.findByLobId(lobId))
            .orElse(Collections.emptyList());

    if (configs.isEmpty()) {
        throw badRequest("No storage configuration exists for lobId=" + lobId);
    }

    if (configs.size() == 1) {
        return configs.get(0);
    }

    // Multiple configs → narrow down using storage + repo
    String storageSystem = "????"; // temporary placeholder
    String repoId = ingestRequest.getStorage().getRepoId();

    return storConfigRepo
            .findByLobIdAndStorageSystemAndRepoId(lobId, storageSystem, repoId)
            .orElseThrow(() ->
                    badRequest("No storage configuration found for lobId=" + lobId +
                               ", storageSystem=" + storageSystem +
                               ", repoId=" + repoId)
            );
}



private DgvLmServiceException badRequest(String message) {
    return new DgvLmServiceException(
            new Status(
                    String.valueOf(HttpStatus.BAD_REQUEST.value()),
                    Severity.Error
            ),
            message
    );
}
