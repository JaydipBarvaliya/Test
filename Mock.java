public ResponseEntity<Resource> getDocumentPdfWithStats(
        HttpHeaders httpHeaders,
        String packageId,
        String documentId,
        String lobId) {

    HeaderInfo headerInfo = new HeaderInfo(lobId,
            TransactionType.GET_SINGLE_DOCUMENT.getDescription(),
            httpHeaders);

    headerInfo.setApiRequestStartTime(System.currentTimeMillis());

    String saasUrl = saasValidationTokenService.buildSaasInputInfo(httpHeaders, lobId).getSaasUrl();

    // OneSpan returns PDF as byte[]
    ResponseEntity<byte[]> responseEntity =
            eslGateway.getDocumentPdf(httpHeaders, packageId, documentId, saasUrl, false);

    log.debug("Document PDF with stats completed successfully");

    // Convert byte[] to Spring Resource
    byte[] pdfBytes = responseEntity.getBody();
    Resource resource = new ByteArrayResource(pdfBytes);

    // Build final response
    ResponseEntity<Resource> finalResponse = ResponseEntity
            .status(responseEntity.getStatusCode())
            .headers(responseEntity.getHeaders())
            .contentType(responseEntity.getHeaders().getContentType())
            .body(resource);

    // Persist stats as you already do
    headerInfoMapper.populateHeaderInfo(headerInfo, finalResponse,
            TransactionType.GET_SINGLE_DOCUMENT.getShortForm());
    persistStats(headerInfo, responseEntity, finalResponse);

    return finalResponse;
}