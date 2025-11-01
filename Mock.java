@ExtendWith(MockitoExtension.class)
class CheckMandatoryPropertyTest {

    @Mock
    ConfigurationProperties config;

    @InjectMocks
    CheckMandatoryProperty checkMandatoryProperty =
            new CheckMandatoryProperty(config, "API_KEY"); // inject with mandatoryProp

    @BeforeEach
    void setUp() {
        checkMandatoryProperty.init(); // run init manually
    }

    @Test
    void testCheckMandatoryProp_ReturnsNull_WhenPropertyIsNotMandatory() throws Exception {
        String lobId = "dna";
        String propToCheck = "NON_MANDATORY_PROP";

        when(config.getConfigProperty(lobId, propToCheck)).thenReturn(null);

        String result = checkMandatoryProperty.checkMandatoryProp(lobId, propToCheck);

        Assertions.assertNull(result);
    }

    @Test
    void testCheckMandatoryProp_ThrowsException_WhenMandatoryValueIsNull() {
        String lobId = "dna";
        String propToCheck = "API_KEY"; // assume this is mandatory

        when(config.getConfigProperty(lobId, propToCheck)).thenReturn(null);

        SharedServiceLayerException thrown = Assertions.assertThrows(
                SharedServiceLayerException.class,
                () -> checkMandatoryProperty.checkMandatoryProp(lobId, propToCheck),
                "Expected to throw SharedServiceLayerException"
        );

        Assertions.assertTrue(thrown.getMessage().contains("Mandatory Config Property"));
    }
}