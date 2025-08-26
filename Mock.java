package <your.test.package>;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class EsignatureeventsApiDelegateImpl_CatchBlocks_NewScreenshots_Test {

    @Mock PackageService packageService;
    @Mock DocumentService documentService;
    @Mock SignerService signerService;
    @Mock PackageManagerUtil packageManagerUtil;
    @Mock AuditTrailResponseMapper auditTrailResponseMapper;

    @InjectMocks EsignatureeventsApiDelegateImpl esignatureeventsApiDelegateImpl;

    @Test
    void lockSigner_throwsSharedServiceLayerException() {
        String eventId = "evt-1";
        String partyId = "party-1";
        String lobId = "lob-1";
        String messageId = "msg-1";
        String traceId = "trace-1";
        AddLockEventRq addLockEventRq = Mockito.mock(AddLockEventRq.class);

        EsignatureeventsApiDelegateImpl spy = Mockito.spy(esignatureeventsApiDelegateImpl);
        HttpHeaders headers = new HttpHeaders();
        Mockito.doReturn(headers).when(spy).buildHeaders(lobId, messageId, traceId);
        Mockito.when(packageManagerUtil.getLobFromHeader(headers)).thenReturn("dna");

        Status status = new Status("500", Severity.Error);
        SharedServiceLayerException boom = new SharedServiceLayerException(status);

        Mockito.doThrow(boom).when(signerService)
                .unLockSigner(headers, eventId, partyId, "dna");

        SharedServiceLayerException ex = Assertions.assertThrows(
                SharedServiceLayerException.class,
                () -> spy.LockSigner(eventId, partyId, lobId, messageId, traceId, addLockEventRq)
        );
        Assertions.assertEquals("500", ex.getStatus().getServerStatusCode());
    }

    @Test
    void sendEmail_throwsSharedServiceLayerException() {
        String eventId = "evt-1";
        String partyId = "party-1";
        String lobId = "lob-1";
        String messageId = "msg-1";
        String traceId = "trace-1";

        EsignatureeventsApiDelegateImpl spy = Mockito.spy(esignatureeventsApiDelegateImpl);
        HttpHeaders headers = new HttpHeaders();
        Mockito.doReturn(headers).when(spy).buildHeaders(lobId, messageId, traceId);
        Mockito.when(packageManagerUtil.getLobFromHeader(headers)).thenReturn("dna");

        Status status = new Status("500", Severity.Error);
        SharedServiceLayerException boom = new SharedServiceLayerException(status);

        Mockito.doThrow(boom).when(signerService)
                .sendSignerReminder(headers, eventId, partyId, "dna");

        SharedServiceLayerException ex = Assertions.assertThrows(
                SharedServiceLayerException.class,
                () -> spy.sendEmail(eventId, partyId, lobId, messageId, traceId)
        );
        Assertions.assertEquals("500", ex.getStatus().getServerStatusCode());
    }

    @Test
    void signs_throwsSharedServiceLayerException() {
        String eventId = "evt-1";
        String email = "test@example.com";
        String lobId = "lob-1";
        String messageId = "msg-1";
        String traceId = "trace-1";
        AddSignsRq addSignsRq = Mockito.mock(AddSignsRq.class);
        Mockito.when(addSignsRq.getSignatureImage()).thenReturn(new byte[0]);

        EsignatureeventsApiDelegateImpl spy = Mockito.spy(esignatureeventsApiDelegateImpl);
        HttpHeaders headers = new HttpHeaders();
        Mockito.doReturn(headers).when(spy).buildHeaders(lobId, messageId, traceId);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        Mockito.when(packageManagerUtil.getLobFromHeader(headers)).thenReturn("dna");

        Status status = new Status("500", Severity.Error);
        SharedServiceLayerException boom = new SharedServiceLayerException(status);

        Mockito.doThrow(boom).when(signerService)
                .createApplySignatures(eq(headers), eq(eventId), eq(email), eq("dna"), any());

        SharedServiceLayerException ex = Assertions.assertThrows(
                SharedServiceLayerException.class,
                () -> spy.signs(eventId, email, lobId, messageId, traceId, addSignsRq)
        );
        Assertions.assertEquals("500", ex.getStatus().getServerStatusCode());
    }

    @Test
    void getEvidenceSummary_throwsSharedServiceLayerException_JSONPath() {
        String eventId = "evt-1";
        String lobId = "lob-1";
        String messageId = "msg-1";
        String traceId = "trace-1";

        EsignatureeventsApiDelegateImpl spy = Mockito.spy(esignatureeventsApiDelegateImpl);
        HttpHeaders headers = new HttpHeaders();
        Mockito.doReturn(headers).when(spy).buildHeaders(lobId, messageId, traceId);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        Mockito.when(packageManagerUtil.getLobFromHeader(headers)).thenReturn("dna");

        Status status = new Status("500", Severity.Error);
        SharedServiceLayerException boom = new SharedServiceLayerException(status);

        Mockito.doThrow(boom).when(packageService)
                .getEvidenceJson(headers, eventId, "dna");

        Assertions.assertThrows(
                SharedServiceLayerException.class,
                () -> spy.getEvidenceSummary(eventId, lobId, messageId, traceId)
        );
    }

    @Test
    void updateTransaction_throwsSharedServiceLayerException() {
        String eventId = "evt-1";
        String lobId = "lob-1";
        String messageId = "msg-1";
        String traceId = "trace-1";
        PartialUpdateESignatureEventRq rq = Mockito.mock(PartialUpdateESignatureEventRq.class);
        HeaderInfo headerInfo = Mockito.mock(HeaderInfo.class);

        EsignatureeventsApiDelegateImpl spy = Mockito.spy(esignatureeventsApiDelegateImpl);
        HttpHeaders headers = new HttpHeaders();
        Mockito.doReturn(headers).when(spy).buildHeaders(lobId, messageId, traceId);
        Mockito.when(packageManagerUtil.getUpdatedHeadersInfo(headers)).thenReturn(headerInfo);
        Mockito.when(packageManagerUtil.getLobFromHeader(headers)).thenReturn("dna");

        Status status = new Status("500", Severity.Error);
        SharedServiceLayerException boom = new SharedServiceLayerException(status);

        Mockito.doThrow(boom).when(packageService)
                .updatePackage(eq(headers), eq(eventId), eq(rq), eq(true), eq("dna"));

        SharedServiceLayerException ex = Assertions.assertThrows(
                SharedServiceLayerException.class,
                () -> spy.updateTransaction(eventId, lobId, messageId, traceId, rq)
        );
        Assertions.assertEquals("500", ex.getStatus().getServerStatusCode());
    }

    @Test
    void deleteTransaction_throwsSharedServiceLayerException() {
        String eventId = "evt-1";
        String lobId = "lob-1";
        String messageId = "msg-1";
        String traceId = "trace-1";
        HeaderInfo headerInfo = Mockito.mock(HeaderInfo.class);

        EsignatureeventsApiDelegateImpl spy = Mockito.spy(esignatureeventsApiDelegateImpl);
        HttpHeaders headers = new HttpHeaders();
        Mockito.doReturn(headers).when(spy).buildHeaders(lobId, messageId, traceId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Mockito.when(packageManagerUtil.getUpdatedHeadersInfo(headers)).thenReturn(headerInfo);
        Mockito.when(packageManagerUtil.getLobFromHeader(headers)).thenReturn("dna");

        Status status = new Status("500", Severity.Error);
        SharedServiceLayerException boom = new SharedServiceLayerException(status);

        Mockito.doThrow(boom).when(packageService)
                .deletePackage(headers, eventId, "dna");

        Assertions.assertThrows(
                SharedServiceLayerException.class,
                () -> spy.deleteTransaction(eventId, lobId, messageId, traceId)
        );
    }
}