catch (WebClientResponseException.NotFound ex) {
    throw new DgvlmServiceException(
            new Status(String.valueOf(HttpStatus.BAD_REQUEST.value()), Severity.Error),
            "Drawer or Folder not found in DG"
    );
}

catch (WebClientResponseException ex) {
    throw new DgvlmServiceException(
            new Status(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value()), Severity.Error),
            "DG returned error: " + ex.getStatusCode()
    );
}

catch (Exception ex) {
    throw new DgvlmServiceException(
            new Status(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value()), Severity.Error),
            "DG system unavailable"
    );
}