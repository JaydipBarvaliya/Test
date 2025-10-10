try (MockedStatic<CryptoUtil> mocked = mockStatic(CryptoUtil.class)) {
    mocked.when(() -> CryptoUtil.decrypt(anyString(), anyString()))
          .thenReturn("decryptedData");

    when(eslGateway.createSessionTokenForSaas(any(), eq(saasUrl), anyString()))
        .thenReturn(dummyResponse);
    when(mandatePropChecker.checkMandatoryProp(eq(lobId), anyString(), anyString()))
        .thenReturn("encryptedData");

    ResponseEntity<String> response = tokenCacheService.generateAccessToken(saasUrl, lobId);

    assertThat(response).isNotNull();
    assertThat(response.getBody()).contains("abc123");
    verify(eslGateway, times(1))
        .createSessionTokenForSaas(any(), eq(saasUrl), anyString());
}