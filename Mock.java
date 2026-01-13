public <T> T post(
    String url,
    Object request,
    Class<T> responseType,
    Map<String, String> headers
) {
    return webClient
        .post()
        .uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .headers(h -> headers.forEach(h::set))
        .bodyValue(request)
        .retrieve()
        .bodyToMono(responseType)
        .timeout(Duration.ofSeconds(timeoutInSeconds))
        .block();
}