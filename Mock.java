try {
    log.info("EsignatureeventsApiDelegateImpl.getEvidenceSummary(): {}",
            LogSanitizeUtil.sanitizeLogObj(eventId));

    HttpHeaders httpHeaders = buildHeaders(lobid, messageID, traceabilityID);

    // Default to PDF if Accept header not provided
    String acceptType = httpHeaders.getAccept().isEmpty()
            ? MediaType.APPLICATION_PDF_VALUE
            : httpHeaders.getAccept().get(0).toString();

    if (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(acceptType)) {
        // Serve JSON
        ResponseEntity<String> response = packageService.getEvidenceJson(
                httpHeaders, eventId, packageManagerUtil.getLobFromHeader(httpHeaders));
        auditTrailResponse = auditTrailResponseMapper.mapResponse(response, acceptType);
    } else {
        // Serve PDF
        auditTrailResponse = packageService.getEvidencePdf(
                httpHeaders, eventId, MediaType.APPLICATION_PDF);
    }

} catch (SharedServiceLayerException e) {
    throw new SharedServiceLayerException(e.getStatus(), e.getMessage(), e.getCause());
}