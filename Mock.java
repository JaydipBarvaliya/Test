@Test
@TAE0141
void testCheckMandatoryPropWhenNull() throws Exception {
    String lobId = "dna";
    String propToCheck = "API_KEY"; // must exist in mandatoryPropList

    // Mock config to return null when fetching this mandatory property
    when(config.getConfigProperty(lobId, propToCheck)).thenReturn(null);

    // Expect SharedServiceLayerException to be thrown
    SharedServiceLayerException ex = assertThrows(
        SharedServiceLayerException.class,
        () -> checkMandatoryProperty.checkMandatoryProp(lobId, propToCheck)
    );

    // Validate exception message
    assertEquals("Mandatory Config Property: API_KEY cannot be null", ex.getMessage());
}