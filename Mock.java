@Test
void getDocuments_throwsSharedServiceLayerException() {
    String eventId = "evt-1";
    String lobId = "lob-1";
    String messageId = "msg-1";
    String traceId = "trace-1";

    EsignatureeventsApiDelegateImpl spy = Mockito.spy(esignatureeventsApiDelegateImpl);
    HttpHeaders headers = new HttpHeaders();
    Mockito.doReturn(headers).when(spy).buildHeaders(lobId, messageId, traceId);

    Mockito.when(packageManagerUtil.getLobFromHeader(headers)).thenReturn("dna");

    Status status = new Status("500", Severity.Error);
    SharedServiceLayerException boom = new SharedServiceLayerException(status);

    Mockito.doThrow(boom).when(documentService)
            .getDocumentsZip(headers, eventId, "dna");

    SharedServiceLayerException ex = Assertions.assertThrows(
            SharedServiceLayerException.class,
            () -> spy.getDocuments(eventId, lobId, messageId, traceId)
    );

    Assertions.assertEquals("500", ex.getStatus().getServerStatusCode());
}