import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class EsignatureeventsApiDelegateImpl_GetDocument_Test {

    @Mock DocumentService documentService;            // whatever the real type is
    @Mock PackageManagerUtil packageManagerUtil;

    @InjectMocks EsignatureeventsApiDelegateImpl sut; // Class under test

    @Test
    void getDocument_throwsSharedServiceLayerException_and_covers_catch() {
        // Arrange
        String eventId = "evt-1";
        String documentId = "doc-1";
        String lobId = "lob-1";
        String messageId = "msg-1";
        String traceId = "trace-1";

        // If buildHeaders is non-final/non-private, use a spy to control it.
        EsignatureeventsApiDelegateImpl spy = Mockito.spy(sut);
        HttpHeaders headers = new HttpHeaders();
        try {
            Mockito.doReturn(headers)
                   .when(spy).buildHeaders(lobId, messageId, traceId);
        } catch (Exception ignore) {
            // in case buildHeaders declares checked exceptions
        }

        // the code reads LOB from headers; give it a value
        Mockito.when(packageManagerUtil.getLobFromHeader(headers)).thenReturn("dna");

        // Prepare the exception that should be caught and rethrown
        Status status = new Status("500", Severity.Error); // use your real ctor
        SharedServiceLayerException boom = new SharedServiceLayerException(status);

        // The key stub: make the *document service* throw
        Mockito.doThrow(boom).when(documentService)
                .getDocumentPdfWithStats(eq(headers), eq(eventId), eq(documentId), eq("dna"));

        // Act + Assert
        SharedServiceLayerException ex = Assertions.assertThrows(
            SharedServiceLayerException.class,
            () -> spy.getDocument(eventId, documentId, lobId, messageId, traceId)
        );

        // Optional: verify we rethrew with the same status/message (as your catch does)
        Assertions.assertEquals("500", ex.getStatus().getServerStatusCode());
        Mockito.verify(documentService).getDocumentPdfWithStats(eq(headers), eq(eventId), eq(documentId), eq("dna"));
    }
}