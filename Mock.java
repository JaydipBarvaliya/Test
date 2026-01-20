private String extractBearerToken(HttpHeaders headers) {
    String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
    if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
        throw new DgvlmServiceException("Invalid Authorization header");
    }
    return authHeader.substring(7);
}