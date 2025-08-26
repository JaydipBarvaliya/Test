@Test
void getAuthenticatedSigningCeremonyURL_throwsSharedServiceLayerException() {
    String eventId = "evt-1";
    String email = "test@example.com";
    String lobId = "lob-1";
    String messageId = "msg-1";
    String traceId = "trace-1";

    EsignatureeventsApiDelegateImpl spy = Mockito.spy(esignatureeventsApiDelegateImpl);
    HttpHeaders headers = new HttpHeaders();
    Mockito.doReturn(headers).when(spy).buildHeaders(lobId, messageId, traceId);

    Mockito.when(packageManagerUtil.getLobFromHeader(headers)).thenReturn("dna");

    Status status = new Status("500", Severity.Error);
    SharedServiceLayerException boom = new SharedServiceLayerException(status);

    Mockito.doThrow(boom).when(signerService)
            .createSingleSessionSignerAuthenticationUrl(headers, eventId, email, "dna");

    SharedServiceLayerException ex = Assertions.assertThrows(
            SharedServiceLayerException.class,
            () -> spy.getAuthenticatedSigningCeremonyURL(eventId, email, lobId, messageId, traceId)
    );

    Assertions.assertEquals("500", ex.getStatus().getServerStatusCode());
}