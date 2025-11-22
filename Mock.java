public ResponseEntity<Resource> getDocumentPdfWithStats(
        HttpHeaders httpHeaders,
        String packageId,
        String documentId,
        String lobId
) {

    HeaderInfo headerInfo = new HeaderInfo(
            lobId,
            TransactionType.GET_SINGLE_DOCUMENT.getDescription(),
            httpHeaders
    );
    headerInfo.setApiRequestStartTime(System.currentTimeMillis());

    String saasUrl = saasValidationTokenService
            .buildSaasInputInfo(httpHeaders, lobId)
            .getSaasUrl();

    // OneSpan returns byte[]
    ResponseEntity<byte[]> upstream =
            eslGateway.getDocumentPdf(
                    httpHeaders,
                    packageId,
                    documentId,
                    saasUrl,
                    false
            );

    // Map to Resource
    ResponseEntity<Resource> finalResponse =
            getSignResponseMapper.mapPdfResponse(
                    upstream,
                    documentId + ".pdf"
            );

    log.debug("Document PDF with stats completed successfully");

    headerInfoMapper.populateHeaderInfo(
            headerInfo,
            finalResponse,
            TransactionType.GET_SINGLE_DOCUMENT.getShortForm()
    );

    persistStats(headerInfo, upstream, finalResponse);

    return finalResponse;
}