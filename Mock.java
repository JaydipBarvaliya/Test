public List<ConfigurationDto> loadConfigurationFromDB(String appId) {
    try {
        return configurationService.findByAppId(List.of(appId, "shared"));
    } catch (DataAccessException e) {
        log.error("DB error while loading config for appId={}", appId, e);
        throw e;
    } catch (Exception e) {
        log.error("Unexpected error while loading config for appId={}", appId, e);
        throw new IllegalStateException("Failed to load configuration from DB", e);
    }
}