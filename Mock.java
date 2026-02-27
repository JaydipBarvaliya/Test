public void validateDrawerAndFolder(String lobId,
                                    String traceabilityId,
                                    IngestRequest ingestRequest)
        throws ApiConfigException, ApiException, DgvLmServiceException {

    String drawerId = ingestRequest.getDigitalVault().getDrawerId();
    String folderId = ingestRequest.getDigitalVault().getFolderId();

    log.debug("Validating drawer and folder. traceabilityId={}, lobId={}, drawerId={}, folderId={}",
            traceabilityId, lobId, drawerId, folderId);

    String pingfedToken = pingFedService.getOauth2ClientSecondaryToken();

    try {
        webClientGateway.callDgvlaToValidateDrawerIdOrFolderId(
                drawerId, folderId, lobId, traceabilityId, pingfedToken);

        log.debug("Drawer and folder validation successful. traceabilityId={}, drawerId={}, folderId={}",
                traceabilityId, drawerId, folderId);

    } catch (Exception ex) {

        log.error("Drawer/Folder validation failed. traceabilityId={}, lobId={}, drawerId={}, folderId={}, error={}",
                traceabilityId, lobId, drawerId, folderId, ex.getMessage(), ex);

        throw new DgvLmServiceException(
                new Status(String.valueOf(HttpStatus.BAD_REQUEST.value()), Severity.Error),
                "Invalid drawerId or folderId.");
    }
}