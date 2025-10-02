@Cacheable(value = {"token"}, key = "#lobId")
public TokenResponse generateAccessToken(String saasUrl, String lobId) {
    ResponseEntity<String> responseEntity =
        this.eslGateway.createSessionTokenForSaas(httpHeaders, saasUrl, mapAccessTokenRequest(lobId));

    return new TokenResponse(responseEntity.getStatusCodeValue(), responseEntity.getBody());
}