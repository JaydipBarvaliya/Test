@Component
public class ESignatureEventGetSignatureResMapper {

    public ResponseEntity<Resource> mapPdfResponse(
            ResponseEntity<byte[]> upstream,
            String fileName
    ) {

        // Same logic as your old mapResponse: only handle 200 OK
        if (upstream == null ||
            !upstream.getStatusCode().is2xxSuccessful() ||
            upstream.getBody() == null) {

            return ResponseEntity
                    .status(
                        upstream != null
                            ? upstream.getStatusCode()
                            : HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .build();
        }

        byte[] pdfBytes = upstream.getBody();

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=" + fileName);

        headers.setContentLength(pdfBytes.length);

        // Content-Type: preserve from OneSpan OR default to PDF
        MediaType contentType = upstream.getHeaders().getContentType();
        if (contentType == null) {
            contentType = MediaType.APPLICATION_PDF;
        }
        headers.setContentType(contentType);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}