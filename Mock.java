package <your.test.package>;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class FormFieldAPIGateway_AllCatchBlocks_Test {

    @Mock RestTemplate restTemplate;
    @Mock CheckMandatoryProperty mandatePropChecker;

    @InjectMocks FormFieldAPIGateway formFieldAPIGateway;

    private HttpEntity<String> mkRequest() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>("{\"k\":\"v\"}", h);
    }

    @BeforeEach
    void init() {
        // no-op, using MockitoExtension
    }

    @Test
    void convertContents_returnsOK_whenResponseOK() throws Exception {
        HttpEntity<String> req = mkRequest();
        Mockito.when(mandatePropChecker.checkMandatoryProp(anyString(), anyString()))
               .thenReturn("https://formfield-api.dev.td.com/api/ESigApplicator");
        ResponseEntity<String> upstream = new ResponseEntity<>("OK", HttpStatus.OK);
        Mockito.when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
               .thenReturn(upstream);

        ResponseEntity<String> result = formFieldAPIGateway.convertContents(req, "lobId");

        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        org.junit.jupiter.api.Assertions.assertEquals("OK", result.getBody());
    }

    @Test
    void convertContents_handlesNullResponse() throws Exception {
        HttpEntity<String> req = mkRequest();
        Mockito.when(mandatePropChecker.checkMandatoryProp(anyString(), anyString()))
               .thenReturn("https://formfield-api.dev.td.com/api/ESigApplicator");
        Mockito.when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
               .thenReturn(null);

        ResponseEntity<String> result = formFieldAPIGateway.convertContents(req, "lobId");

        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        org.junit.jupiter.api.Assertions.assertTrue(result.getBody().contains("No response from Form Field API"));
    }

    @Test
    void convertContents_catchesUriSyntaxException() throws Exception {
        HttpEntity<String> req = mkRequest();
        Mockito.when(mandatePropChecker.checkMandatoryProp(anyString(), anyString()))
               .thenReturn("ht!tp://bad url");

        ResponseEntity<String> result = formFieldAPIGateway.convertContents(req, "lobId");

        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        org.junit.jupiter.api.Assertions.assertTrue(result.getBody().contains("URISyntaxException"));
    }

    @Test
    void convertContents_catchesHttpServerErrorException() throws Exception {
        HttpEntity<String> req = mkRequest();
        Mockito.when(mandatePropChecker.checkMandatoryProp(anyString(), anyString()))
               .thenReturn("https://formfield-api.dev.td.com/api/ESigApplicator");
        Mockito.when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
               .thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY, "upstream-5xx"));

        ResponseEntity<String> result = formFieldAPIGateway.convertContents(req, "lobId");

        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.BAD_GATEWAY, result.getStatusCode());
        org.junit.jupiter.api.Assertions.assertTrue(result.getBody().contains("HttpServerErrorException"));
    }

    @Test
    void convertContents_catchesHttpStatusCodeException() throws Exception {
        HttpEntity<String> req = mkRequest();
        Mockito.when(mandatePropChecker.checkMandatoryProp(anyString(), anyString()))
               .thenReturn("https://formfield-api.dev.td.com/api/ESigApplicator");
        Mockito.when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
               .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "not-found", new byte[0], StandardCharsets.UTF_8));

        ResponseEntity<String> result = formFieldAPIGateway.convertContents(req, "lobId");

        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        org.junit.jupiter.api.Assertions.assertTrue(result.getBody().contains("HttpStatusCodeException"));
    }

    @Test
    void convertContents_catchesGenericException() throws Exception {
        HttpEntity<String> req = mkRequest();
        Mockito.when(mandatePropChecker.checkMandatoryProp(anyString(), anyString()))
               .thenReturn("https://formfield-api.dev.td.com/api/ESigApplicator");
        Mockito.when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
               .thenThrow(new RuntimeException("boom"));

        ResponseEntity<String> result = formFieldAPIGateway.convertContents(req, "lobId");

        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        org.junit.jupiter.api.Assertions.assertTrue(result.getBody().contains("Exception occured"));
    }
}