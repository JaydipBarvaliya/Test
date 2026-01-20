package com.td.dgvlm.api.util;

import com.td.dgvlm.api.constants.ApiConstants;
import com.td.dgvlm.api.exception.DgvlmServiceException;
import com.td.dgvlm.api.model.Status;
import com.td.dgvlm.api.security.AccessTokenClaims;
import com.td.dgvlm.api.security.OAuthValidator;
import com.td.dgvlm.api.service.ClientAuthConfigurationLoader;
import com.td.dgvlm.api.service.AppConfigurationProperties;
import com.td.dgvlm.api.enums.Severity;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Utility responsible for validating client authorization
 * and client app configuration.
 */
@Slf4j
@Component
public class ClientFieldValidatorUtil {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ClientAuthConfigurationLoader clientAuthConfig;
    private final AppConfigurationProperties configurationProperties;
    private final String jwtSecuredFlag;

    public ClientFieldValidatorUtil(
            ClientAuthConfigurationLoader clientAuthConfig,
            AppConfigurationProperties configurationProperties,
            @Value("${environment.dev.jwt.secured.mode.flag}") String jwtSecuredFlag
    ) {
        this.clientAuthConfig = clientAuthConfig;
        this.configurationProperties = configurationProperties;
        this.jwtSecuredFlag = jwtSecuredFlag;
    }

    /**
     * Validates the client based on Authorization header and LOB.
     */
    public void validateClients(HttpHeaders headers, String lobId) {

        try {
            String token = extractBearerToken(headers);
            AccessTokenClaims accessTokenClaims = resolveAccessTokenClaims(token);

            String adminClientIds =
                    configurationProperties.getConfigProperty(
                            ApiConstants.DEFAULT,
                            ApiConstants.ADMIN_CLIENT_IDS
                    );

            boolean isAdminClient =
                    StringUtils.hasText(adminClientIds)
                            && adminClientIds.contains(accessTokenClaims.getClientId());

            if (!isAdminClient) {
                clientAuthConfig.isClientAppConfigured(
                        accessTokenClaims.getClientId(),
                        lobId
                );
            }

        } catch (DgvlmServiceException e) {
            log.error("Error validating client app configuration: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during client validation: {}", e.getMessage());
            throw new DgvlmServiceException(
                    new Status(String.valueOf(HttpStatus.UNAUTHORIZED.value()), Severity.Error),
                    "Unauthorized client"
            );
        }
    }

    /**
     * Extracts Bearer token from Authorization header.
     */
    private String extractBearerToken(HttpHeaders headers) {
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new DgvlmServiceException(
                    new Status(String.valueOf(HttpStatus.UNAUTHORIZED.value()), Severity.Error),
                    "Invalid Authorization header"
            );
        }

        return authHeader.substring(BEARER_PREFIX.length());
    }

    /**
     * Resolves access token claims based on environment flag.
     */
    private AccessTokenClaims resolveAccessTokenClaims(String token) {
        if (ApiConstants.TRUE.equalsIgnoreCase(jwtSecuredFlag)) {
            log.warn("JWT signature validation disabled (dev/testing mode)");
            return OAuthValidator.getDecodedClaimsWithoutValidation(token);
        }

        log.info("JWT signature validation enabled");
        return OAuthValidator.getValidToken(token);
    }
}