@Test
void testGenerateAccessToken_Success() throws Exception {
    String lobId = "LOB123";
    String saasUrl = "https://saas.example.com";
    ResponseEntity<String> dummyResponse = ResponseEntity.ok("{\"token\":\"abc123\"}");

    ReflectionTestUtils.setField(tokenCacheService, "decryptionKey", "12345678901234567890123456789012");

    when(eslGateway.createSessionTokenForSaas(any(), eq(saasUrl), anyString()))
        .thenReturn(dummyResponse);
    when(mandatePropChecker.checkMandatoryProp(eq(lobId), anyString(), anyString()))
        .thenReturn("encryptedData");

    // ✅ mock the construction of CryptoUtil
    try (MockedConstruction<CryptoUtil> mocked = mockConstruction(CryptoUtil.class,
            (mock, context) -> when(mock.decrypt(anyString(), anyString()))
                    .thenReturn("decryptedData"))) {

        ResponseEntity<String> response = tokenCacheService.generateAccessToken(saasUrl, lobId);

        assertThat(response).isNotNull();
        assertThat(response.getBody()).contains("abc123");
        verify(eslGateway, times(1))
            .createSessionTokenForSaas(any(), eq(saasUrl), anyString());
    }
}