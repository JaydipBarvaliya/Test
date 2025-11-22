public ResponseEntity<Resource> mapPdfResponse(ResponseEntity<byte[]> responseEntity,
                                               String fileToSave) {

    // preserve the old behavior: only care about 200 OK, else return null
    if (!responseEntity.getStatusCode().is2xxSuccessful() ||
        responseEntity.getBody() == null) {
        return null;
    }

    byte[] body = responseEntity.getBody();

    HttpHeaders headers = new HttpHeaders();

    // EXACT same header behavior as old mapResponse
    headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + fileToSave);

    headers.setContentLength(body.length);

    // DO NOT set Content-Type (matches your old logic exactly)
    // Spring will choose based on Accept, just like before.

    // Return same bytes, but wrapped as Resource
    Resource resource = new ByteArrayResource(body);

    return new ResponseEntity<>(resource, headers, HttpStatus.OK);
}