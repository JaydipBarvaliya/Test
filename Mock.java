package com.td.dgvlm.api.service;

import com.td.coreapi.common.oauthsdk.model.OAuthResponse;
import com.td.coreapi.common.oauthsdk.service.OAuthSDKService;
import com.td.coreapi.common.oauthsdk.validator.OAuthValidator;
import com.td.dgvlm.api.exception.ApiConfigException;
import com.td.dgvlm.api.exception.ApiException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PingFedServiceTest {

    @Mock
    private OAuthSDKService oAuthSDKService;

    @InjectMocks
    private PingFedService pingFedService;

    private OAuthResponse response;

    @BeforeEach
    void setUp() {
        response = mock(OAuthResponse.class);
    }

    @Test
    void shouldReturnToken_whenValidAndNotExpired() throws Exception {

        when(response.getAccessToken()).thenReturn("valid-token");
        when(oAuthSDKService.getToken(any())).thenReturn(response);

        try (MockedStatic<OAuthValidator> validatorMock =
                     mockStatic(OAuthValidator.class)) {

            validatorMock.when(() -> OAuthValidator.isExpired("valid-token"))
                    .thenReturn(false);

            String result = pingFedService.getOauth2ClientToken();

            assertEquals("valid-token", result);
        }
    }

    @Test
    void shouldReturnNull_whenTokenExpired() throws Exception {

        when(response.getAccessToken()).thenReturn("expired-token");
        when(oAuthSDKService.getToken(any())).thenReturn(response);

        try (MockedStatic<OAuthValidator> validatorMock =
                     mockStatic(OAuthValidator.class)) {

            validatorMock.when(() -> OAuthValidator.isExpired("expired-token"))
                    .thenReturn(true);

            String result = pingFedService.getOauth2ClientToken();

            assertNull(result);
        }
    }

    @Test
    void shouldReturnNull_whenOAuthResponseIsNull() throws Exception {

        when(oAuthSDKService.getToken(any())).thenReturn(null);

        String result = pingFedService.getOauth2ClientToken();

        assertNull(result);
    }

    @Test
    void shouldThrowApiException_whenApiExceptionOccurs() throws Exception {

        ApiException apiException =
                new ApiException(500, "failure");

        when(oAuthSDKService.getToken(any()))
                .thenThrow(apiException);

        ApiException thrown =
                assertThrows(ApiException.class,
                        () -> pingFedService.getOauth2ClientToken());

        assertEquals(500, thrown.getStatus());
    }

    @Test
    void shouldRethrowGenericException_whenOtherExceptionOccurs() throws Exception {

        when(oAuthSDKService.getToken(any()))
                .thenThrow(new RuntimeException("unexpected"));

        assertThrows(RuntimeException.class,
                () -> pingFedService.getOauth2ClientToken());
    }
}