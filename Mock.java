@ExtendWith(MockitoExtension.class)
class EsignatureeventsApiDelegateImplTest {

    @Mock PackageService packageService;
    @Mock PackageManagerUtil packageManagerUtil;

    @InjectMocks EsignatureeventsApiDelegateImpl sut;  // SUT = class under test

    @Test
    void getTransaction_throwsSharedServiceLayerException() {
        // inputs
        String eventId = "testEventId";
        String lobId = "lob123";
        String messageId = "message123";
        String traceId = "trace123";

        // make a spy so we can stub buildHeaders()
        EsignatureeventsApiDelegateImpl spy = Mockito.spy(sut);
        HttpHeaders headers = new HttpHeaders();
        Mockito.doReturn(headers)
               .when(spy).buildHeaders(lobId, messageId, traceId);

        // stub the other collaborator used in the method
        Mockito.when(packageManagerUtil.getLobFromHeader(headers))
               .thenReturn("dna");

        // prepare the exception the catch block should rethrow
        Status status = new Status("500", Severity.Error);
        SharedServiceLayerException toThrow = new SharedServiceLayerException(status);

        // THIS is where the exception must be thrown
        Mockito.doThrow(toThrow)
               .when(packageService).getPackage(headers, eventId, "dna");

        // assert
        SharedServiceLayerException ex = Assertions.assertThrows(
            SharedServiceLayerException.class,
            () -> spy.getTransaction(eventId, lobId, messageId, traceId)
        );
        Assertions.assertEquals("500", ex.getStatus().getServerStatusCode());
    }
}