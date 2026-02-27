public void validateDrawerAndFolder(
        IngestRequest ingestRequest,
        String traceabilityId) {

    String drawerId = ingestRequest.getDigitalVault().getDrawerId();
    String folderId = ingestRequest.getDigitalVault().getFolderId();
    String token = pingFedService.getOauth2ClientSecondaryToken();

    try {

        webClientGateway.callDgvlToValidateDrawerIdOrFolderId(
                drawerId,
                folderId,
                traceabilityId,
                token
        );

    } catch (Exception ex) {

        throw new DgvlmServiceException(
                new Status(
                        String.valueOf(HttpStatus.BAD_REQUEST.value()),
                        Severity.Error
                ),
                "Drawer or Folder validation failed in DG"
        );
    }
}