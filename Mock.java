import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class CheckMandatoryPropertyTest {

    @Mock
    ConfigurationProperties config;

    @InjectMocks
    CheckMandatoryProperty checkMandatoryProperty;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Ensure the private field `mandateProp` is initialized BEFORE init()
        ReflectionTestUtils.setField(checkMandatoryProperty, "mandateProp", "API_KEY,AUTH_TOKEN,CLIENT_ID");
        checkMandatoryProperty.init(); // Call init after setting mandateProp
    }

    @Test
    void testCheckMandatoryProp() throws Exception {
        String lobId = "dna";
        String propToCheck = "API_KEY";

        // Optional mock setup (depends on your ConfigurationProperties logic)
        when(config.getConfigProperty(lobId, propToCheck)).thenReturn(null);

        String result = checkMandatoryProperty.checkMandatoryProp(lobId, propToCheck);
        assertNull(result); // assert logic based on your expected result
    }

    @Test
    void testCheckMandatoryPropWhenNull() throws Exception {
        String lobId = "dna";
        String propToCheck = "NON_EXISTING_PROP";

        when(config.getConfigProperty(lobId, propToCheck)).thenReturn(null);

        String result = checkMandatoryProperty.checkMandatoryProp(lobId, propToCheck);
        assertNull(result);
    }
}