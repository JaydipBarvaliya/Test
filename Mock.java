@Test
void shouldReturnToken_whenValidAndNotExpired() throws Exception {

    OAuthRequest mockRequest = mock(OAuthRequest.class);
    OAuthResponse mockResponse = mock(OAuthResponse.class);

    OAuthRequestBuilder.ClientCredentialsRequest clientCredStage =
            mock(OAuthRequestBuilder.ClientCredentialsRequest.class);

    try (MockedConstruction<OAuthRequestBuilder> mocked =
                 mockConstruction(OAuthRequestBuilder.class,
                         (builderMock, context) -> {

                             when(builderMock.withClientCredentialsRequest())
                                     .thenReturn(clientCredStage);

                             when(clientCredStage.endClientCredentialsRequest())
                                     .thenReturn(builderMock);

                             when(builderMock.build())
                                     .thenReturn(mockRequest);
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