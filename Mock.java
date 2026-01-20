@Component
@Slf4j
@RequiredArgsConstructor
public class ClientAuthConfigurationLoader {

    private final ClientAuthConfigurationService clientAuthConfigurationService;

    private final Map<String, Set<String>> clientIdMapping = new ConcurrentHashMap<>();

    public enum Text {
        CLIENT_ID,
        LOB_ID,
        MISSING_CLIENT_ID,
        MISSING_BOTH
    }

    @PostConstruct
    public void init() {
        List<ClientAuthConfigurationDto> configurations =
                clientAuthConfigurationService.fetchAllData();

        for (ClientAuthConfigurationDto config : configurations) {
            clientIdMapping
                    .computeIfAbsent(config.getClientId(), k -> new HashSet<>())
                    .add(config.getLob());
        }

        log.info("Loaded ClientAuth configuration for {} clients", clientIdMapping.size());
    }

    /**
     * Validates whether clientId + lobId is configured.
     * Returns normally if valid, throws exception otherwise.
     */
    public void validateClientAppConfigured(String clientId, String lobId)
            throws DgvlmServiceException {

        if (!StringUtils.hasText(clientId) && !StringUtils.hasText(lobId)) {
            throwException(null, Text.MISSING_BOTH);
        }

        if (!StringUtils.hasText(clientId)) {
            throwException(null, Text.MISSING_CLIENT_ID);
        }

        if (!StringUtils.hasText(lobId)) {
            throwException(clientId, Text.LOB_ID);
        }

        Set<String> lobs = clientIdMapping.get(clientId);
        if (lobs == null || !lobs.contains(lobId)) {
            throwException(clientId + " and Lob ID: " + lobId, Text.MISSING_BOTH);
        }
    }

    private void throwException(String value, Text text) throws DgvlmServiceException {
        Status status;

        switch (text) {
            case CLIENT_ID -> {
                status = new Status(
                        String.valueOf(HttpStatus.UNAUTHORIZED.value()),
                        Severity.Error
                );
                throw new DgvlmServiceException(
                        status,
                        "ClientId is not configured: " + value
                );
            }

            case LOB_ID -> {
                status = new Status(
                        String.valueOf(HttpStatus.BAD_REQUEST.value()),
                        Severity.Error
                );
                throw new DgvlmServiceException(
                        status,
                        "Lob ID is required."
                );
            }

            case MISSING_CLIENT_ID -> {
                status = new Status(
                        String.valueOf(HttpStatus.BAD_REQUEST.value()),
                        Severity.Error
                );
                throw new DgvlmServiceException(
                        status,
                        "Client ID is required."
                );
            }

            default -> {
                status = new Status(
                        String.valueOf(HttpStatus.UNAUTHORIZED.value()),
                        Severity.Error
                );
                throw new DgvlmServiceException(
                        status,
                        "Client Application is not configured for the given Client ID: " + value
                );
            }
        }
    }
}