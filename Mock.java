public void validateClientApp(Optional<NativeWebRequest> requestOpt, String lobId) throws DgvlmServiceException {
    HttpHeaders headers = new HttpHeaders();

    requestOpt.ifPresent(req -> {
        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null && !auth.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, auth);
        }
    });

    validateClients(headers, lobId);
}