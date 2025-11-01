@Test
@TAE0141
void testCheckMandatoryPropWhenNull() throws Exception {
    String lobId = "dna";
    String propToCheck = null;

    // Mock config and isMandatory behaviour
    when(config.getConfigProperty(lobId, propToCheck)).thenReturn(null);
    CheckMandatoryProperty spyCheckMandatoryProperty = Mockito.spy(checkMandatoryProperty);
    Mockito.doReturn(true).when(spyCheckMandatoryProperty).isMandatory(propToCheck);

    // Expect SharedServiceLayerException to be thrown
    SharedServiceLayerException ex = assertThrows(
        SharedServiceLayerException.class,
        () -> spyCheckMandatoryProperty.checkMandatoryProp(lobId, propToCheck)
    );

    assertEquals("Mandatory Config Property: null cannot be null", ex.getMessage());
}