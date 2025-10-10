package com.td.esig.tokengeneration.service;

import com.td.esig.common.util.*;
import com.td.esig.model.v1.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.cache.annotation.Cacheable;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenCacheServiceTest {

    @Mock
    private TokenEsIGateway eslGateway;

    @Mock
    private TokenPropertyCheck mandatePropChecker;

    @InjectMocks
    private TokenCacheService tokenCacheService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Set mock decryption key
        tokenCacheService.decryptionKey = "dummyKey";
    }

    @Test
    void testGenerateAccessToken_Success() throws Exception {
        // given
        String lobId = "LOB123";
        String saasUrl = "https://saas.example.com";
        ResponseEntity<String> dummyResponse = ResponseEntity.ok("{\"token\":\"abc123\"}");

        when(eslGateway.createSessionTokenForSaas(any(), eq(saasUrl), anyString()))
                .thenReturn(dummyResponse);
        when(mandatePropChecker.checkMandatoryProp(eq(lobId), anyString(), anyString()))
                .thenReturn("encryptedData");

        // when
        ResponseEntity<String> response = tokenCacheService.generateAccessToken(saasUrl, lobId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getBody()).contains("abc123");
        verify(eslGateway, times(1)).createSessionTokenForSaas(any(), eq(saasUrl), anyString());
    }

    @Test
    void testGenerateAccessToken_Exception() throws Exception {
        // given
        String lobId = "LOB456";
        String saasUrl = "https://saas.example.com";
        when(mandatePropChecker.checkMandatoryProp(anyString(), anyString(), anyString()))
                .thenThrow(new SharedServiceLayerException(new Status(500, "FAIL")));

        // when / then
        assertThrows(SharedServiceLayerException.class, () ->
                tokenCacheService.generateAccessToken(saasUrl, lobId));
    }

    @Test
    void testMapAccessTokenRequest_Success() throws Exception {
        // given
        String lobId = "LOB789";
        when(mandatePropChecker.checkMandatoryProp(eq(lobId), anyString(), anyString()))
                .thenReturn("encrypted");
        // no exception path
        AccessTokenRequest request = tokenCacheService.mapAccessTokenRequest(lobId);

        assertThat(request).isNotNull();
        assertThat(request.getType()).isEqualTo(SaasConstant.OWNER);
    }

    @Test
    void testMapAccessTokenRequest_Exception() throws Exception {
        String lobId = "LOB_FAIL";
        when(mandatePropChecker.checkMandatoryProp(anyString(), anyString(), anyString()))
                .thenThrow(new SharedServiceLayerException(new Status(500, "Decryption failed")));

        assertThrows(SharedServiceLayerException.class, () ->
                tokenCacheService.mapAccessTokenRequest(lobId));
    }

    @Test
    void testCacheableAnnotationPresent() throws Exception {
        // Reflection check just for coverage
        Cacheable cacheable = TokenCacheService.class
                .getDeclaredMethod("generateAccessToken", String.class, String.class)
                .getAnnotation(Cacheable.class);
        assertThat(cacheable).isNotNull();
        assertThat(cacheable.value()[0]).isEqualTo("token");
    }
}