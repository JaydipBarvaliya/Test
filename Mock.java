try {
    log.info("EsignatureeventsApiDelegateImpl.getEvidenceSummary(): {}",
            LogSanitizeUtil.sanitizeLogObj(eventId));

    HttpHeaders httpHeaders = buildHeaders(lobid, messageID, traceabilityID);

    // Get Accept header or default to PDF
    String acceptType = httpHeaders.getAccept().isEmpty()
            ? MediaType.APPLICATION_PDF_VALUE
            : httpHeaders.getAccept().get(0).toString();

    if (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(acceptType)) {
        // Serve JSON
        ResponseEntity<String> response = packageService.getEvidenceJson(
                httpHeaders, eventId, packageManagerUtil.getLobFromHeader(httpHeaders));
        auditTrailResponse = auditTrailResponseMapper.mapResponse(response, acceptType);

    } else if (MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(acceptType)) {
        // Serve PDF
        auditTrailResponse = packageService.getEvidencePdf(
                httpHeaders, eventId, MediaType.APPLICATION_PDF);

    } else {
        // Unsupported type
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(statusFrom("Only application/json or application/pdf are supported"));
    }

} catch (SharedServiceLayerException e) {
    throw new SharedServiceLayerException(e.getStatus(), e.getMessage(), e.getCause());
}