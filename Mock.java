public ResponseEntity<?> mapResponse(ResponseEntity<?> responseEntity, String fileToSave, String acceptHeader) {

    if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null) {
        
        byte[] bytes = (byte[]) responseEntity.getBody();
        HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileToSave);

        // 1. Respect Accept header if present
        if ("application/pdf".equalsIgnoreCase(acceptHeader)) {
            headers.setContentType(MediaType.APPLICATION_PDF);
        }
        else if ("application/octet-stream".equalsIgnoreCase(acceptHeader)) {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }
        else {
            // 2. Default behavior when Accept is missing → return octet-stream
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        headers.setContentLength(bytes.length);

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    return null;
}