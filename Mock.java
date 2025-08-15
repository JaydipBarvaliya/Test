String acceptType = (acceptType == null || acceptType.isBlank())
        ? MediaType.APPLICATION_PDF_VALUE
        : acceptType;

if (MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(acceptType)) {
    // Serve JSON
    auditTrailResponse = packageService.getEvidenceJson(httpHeaders, eventId, MediaType.APPLICATION_JSON);
} else if (MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(acceptType)) {
    // Serve PDF
    auditTrailResponse = packageService.getEvidencePdf(httpHeaders, eventId, MediaType.APPLICATION_PDF);
} else {
    // Unsupported type → 406
    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
            .body(statusFrom("Only application/json or application/pdf are supported"));
}