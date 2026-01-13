public BatchDocResponse callBatchDoc(
    BatchDocRequest request,
    String traceabilityId,
    String bearerToken
) {
    return post(
        batchDocUrl,
        request,
        BatchDocResponse.class,
        Map.of(
            "TraceabilityId", traceabilityId,
            HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken
        )
    );
}