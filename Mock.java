private String resolveBatchDocUrl(String lobId) {
    if (ApiConstants.TDI_CLAIMS_DEV.equals(lobId)) {
        return configurationProperties.getConfigProperty(
            ApiConstants.TDI_CLAIMS_DEV,
            ApiConstants.BATCHDOC_URL
        );
    }
    return configurationProperties.getConfigProperty(
        ApiConstants.DEFAULT,
        ApiConstants.BATCHDOC_URL
    );
}


String batchDocUrl = resolveBatchDocUrl(lobId);
