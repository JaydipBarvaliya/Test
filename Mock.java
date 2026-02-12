@ExtendWith(MockitoExtension.class)
class PingFedServiceTest {

    @Mock
    private OAuthSDKService oAuthSDKService;

    @InjectMocks
    private PingFedService pingFedService;

    @Test
    void shouldReturnToken_whenValidAndNotExpired() throws Exception {

        OAuthRequest mockRequest = mock(OAuthRequest.class);
        OAuthResponse mockResponse = mock(OAuthResponse.class);

        try (MockedConstruction<OAuthRequestBuilder> mocked =
                     mockConstruction(OAuthRequestBuilder.class,
                             (mock, context) -> {

                                 when(mock.withClientCredentialsRequest()).thenReturn(mock);
                                 when(mock.endClientCredentialsRequest()).thenReturn(mock);
                                 when(mock.build()).thenReturn(mockRequest);
                             })) {

            when(oAuthSDKService.getToken(mockRequest))
                    .thenReturn(mockResponse);

            when(mockResponse.getAccessToken())
                    .thenReturn("test-token");

            try (MockedStatic<OAuthValidator> validatorMock =
                         mockStatic(OAuthValidator.class)) {

                validatorMock.when(() ->
                        OAuthValidator.isExpired("test-token"))
                        .thenReturn(false);

                String result = pingFedService.getOauth2ClientToken();

                assertEquals("test-token", result);
            }
        }
    }
}