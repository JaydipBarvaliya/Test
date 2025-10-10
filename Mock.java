package com.td.aesig.tokengeneration.service;

import com.td.esig.common.util.CryptoUtil;
import com.td.esig.common.util.SharedServiceLayerException;
import com.td.esig.tokengeneration.gateway.TokenEslGateway;
import com.td.esig.tokengeneration.util.TokenPropertyCheck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TokenCacheServiceTest {

    @Mock
    private TokenEslGateway eslGateway;

    @Mock
    private TokenPropertyCheck mandatePropChecker;

    @InjectMocks
    private TokenCacheService tokenCacheService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Inject a dummy key so @Value doesn’t complain
        ReflectionTestUtils.setField(tokenCacheService, "decryptionKey",
                "12345678901234567890123456789012"); // 32 chars for AES
    }

    @Test
    void testGenerateAccessToken_Success() throws Exception {
        // given
        String lobId = "LOB123";
        String saasUrl = "https://saas.example.com";
        ResponseEntity<String> dummyResponse =
                ResponseEntity.ok("{\"token\":\"abc123\"}");

        // Mock dependent services
        when(eslGateway.createSessionTokenForSaas(any(), eq(saasUrl), anyString()))
                .thenReturn(dummyResponse);
        when(mandatePropChecker.checkMandatoryProp(eq(lobId), anyString()))
                .thenReturn("encryptedData");

        // ✅ Mock constructor of CryptoUtil
        try (MockedConstruction<CryptoUtil> mocked = mockConstruction(CryptoUtil.class,
                (mock, context) -> when(mock.decrypt(anyString(), anyString()))
                        .thenReturn("decryptedData"))) {

            // when
            ResponseEntity<String> response =
                    tokenCacheService.generateAccessToken(saasUrl, lobId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getBody()).contains("abc123");
            verify(eslGateway, times(1))
                    .createSessionTokenForSaas(any(), eq(saasUrl), anyString());
        }
    }

    @Test
    void testGenerateAccessToken_Exception() throws Exception {
        // given
        String lobId = "LOB456";
        String saasUrl = "https://saas.example.com";

        when(mandatePropChecker.checkMandatoryProp(eq(lobId), anyString()))
                .thenThrow(new SharedServiceLayerException("mocked failure"));

        // when / then
        try {
            tokenCacheService.generateAccessToken(saasUrl, lobId);
        } catch (SharedServiceLayerException e) {
            assertThat(e.getMessage()).contains("mocked failure");
        }
    }
}