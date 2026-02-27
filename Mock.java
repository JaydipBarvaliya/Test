public ResponseEntity<Void> get(
        String url,
        Map<String, String> headers) {

    return webClient
            .get()
            .uri(url)
            .headers(httpHeaders -> headers.forEach(httpHeaders::add))
            .retrieve()
            .toBodilessEntity()
            .block(Duration.ofSeconds(timeoutInSeconds));
}



public ResponseEntity<Void> validateFolder(
        String drawerId,
        String folderId,
        String traceabilityId,
        String primaryToken) {

    String url = dgvlUrl + "/drawers/" + drawerId + "/folders/" + folderId;

    return get(
            url,
            Map.of(
                    ApiConstants.TRACEABILITY_ID, traceabilityId,
                    HttpHeaders.AUTHORIZATION, ApiConstants.BEARER_PREFIX + primaryToken
            )
    );
}



private void validateDrawerAndFolder(
        IngestRequest ingestRequest,
        String traceabilityId,
        String primaryToken) {

    String drawerId = ingestRequest.getDigitalVault().getDrawerId();
    String folderId = ingestRequest.getDigitalVault().getFolderId();

    try {
        ResponseEntity<Void> response =
                webClientGateway.validateFolder(
                        drawerId,
                        folderId,
                        traceabilityId,
                        primaryToken
                );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new DgvlmServiceException("Unexpected response from DigiVLA");
        }

    } catch (WebClientResponseException ex) {

        if (ex.getStatusCode().value() == 404) {
            throw new DgvlmServiceException(
                    "Drawer or Folder not found in DigiVLA",
                    HttpStatus.BAD_REQUEST
            );
        }

        throw new DgvlmServiceException(
                "DigiVLA system unavailable",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}


validateDrawerAndFolder(ingestRequest, traceabilityID, primaryToken);


catch (WebClientResponseException.NotFound ex) {
    throw new DgvlmServiceException(
            "Drawer or Folder not found in DigiVLA",
            HttpStatus.BAD_REQUEST
    );
}
catch (WebClientResponseException ex) {
    throw new DgvlmServiceException(
            "DigiVLA returned error: " + ex.getStatusCode(),
            HttpStatus.SERVICE_UNAVAILABLE
    );
}
catch (Exception ex) {
    throw new DgvlmServiceException(
            "DigiVLA system unavailable",
            HttpStatus.SERVICE_UNAVAILABLE
    );
}



