private AccessTokenClaims resolveClaims(HttpHeaders headers) {
    String token = extractBearerToken(headers);

    if (!jwtSecured) {
        log.warn("JWT signature validation disabled");
        return OAuthValidator.getDecodedClaimsWithoutValidation(token);
    }

    return OAuthValidator.getValidToken(token);
}