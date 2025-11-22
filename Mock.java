public ResponseEntity<Resource> mapPdfResponse(
        ResponseEntity<byte[]> oneSpanResponse,
        String fileName
) {
    byte[] pdfBytes = oneSpanResponse.getBody();
    Resource resource = new ByteArrayResource(pdfBytes);

    HttpHeaders headers = new HttpHeaders();
    headers.putAll(oneSpanResponse.getHeaders());

    // optional: override Content-Disposition
    headers.setContentDisposition(
        ContentDisposition.attachment()
            .filename(fileName + ".pdf")
            .build()
    );

    return ResponseEntity
            .status(oneSpanResponse.getStatusCode())
            .headers(headers)
            .body(resource);
}