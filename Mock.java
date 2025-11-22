public ResponseEntity<Resource> getDocumentPdfWithStats(
        HttpHeaders httpHeaders,
        String packageId,
        String documentId,
        String lobId
) {

    HeaderInfo headerInfo = new HeaderInfo(lobId,
            TransactionType.GET_SINGLE_DOCUMENT.getDescription(),
            httpHeaders);

    headerInfo.setApiRequestStartTime(System.currentTimeMillis());

    String saasUrl = saasValidationTokenService
            .buildSaasInputInfo(httpHeaders, lobId)
            .getSaasUrl();

    ResponseEntity<byte[]> oneSpanResponse =
            eslGateway.getDocumentPdf(httpHeaders, packageId, documentId, saasUrl, false);

    // 🔥 your mapper, now correctly returning Resource
    ResponseEntity<Resource> finalResponse =
            getSignResponseMapper.mapPdfResponse(oneSpanResponse, documentId);

    log.debug("Document Pdf with stats completed successfully");

    headerInfoMapper.populateHeaderInfo(headerInfo,
            finalResponse,
            TransactionType.GET_SINGLE_DOCUMENT.getShortForm());

    persistStats(headerInfo, oneSpanResponse, finalResponse);

    return finalResponse;
}