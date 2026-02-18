package com.td.dgvlm.api.delegates;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.NativeWebRequest;

import com.td.dgvlm.api.model.Ingest200Response;
import com.td.dgvlm.api.model.IngestRequest;
import com.td.dgvlm.api.model.IngestTxnRs;
import com.td.dgvlm.domain.StorConfig;
import com.td.dgvlm.domain.StorTransaction;
import com.td.dgvlm.exception.DgvLmServiceException;
import com.td.dgvlm.repository.StorConfigRepository;
import com.td.dgvlm.service.BatchDocAsyncService;
import com.td.dgvlm.service.IngestService;
import com.td.dgvlm.service.TransactionStatusService;
import com.td.dgvlm.util.ClientFieldValidatorUtil;

class IngestApiDelegateImplTest {

    private StorConfigRepository storConfigRepo;
    private TransactionStatusService transService;
    private BatchDocAsyncService batchDocAsyncService;
    private IngestService ingestService;
    private NativeWebRequest nativeWebRequest;
    private ClientFieldValidatorUtil clientFieldValidatorUtil;

    private IngestApiDelegateImpl delegate;

    @BeforeEach
    void setUp() {
        storConfigRepo = mock(StorConfigRepository.class);
        transService = mock(TransactionStatusService.class);
        batchDocAsyncService = mock(BatchDocAsyncService.class);
        ingestService = mock(IngestService.class);
        nativeWebRequest = mock(NativeWebRequest.class);
        clientFieldValidatorUtil = mock(ClientFieldValidatorUtil.class);

        delegate = new IngestApiDelegateImpl(
                storConfigRepo,
                transService,
                batchDocAsyncService,
                ingestService,
                nativeWebRequest,
                clientFieldValidatorUtil
        );
    }

    @Test
    void testGetRequest_present() {
        Optional<NativeWebRequest> result = delegate.getRequest();
        assertTrue(result.isPresent());
        assertSame(nativeWebRequest, result.get());
    }

    @Test
    void testGetRequest_empty_whenNullPassedInCtor() {
        IngestApiDelegateImpl delegateWithNullRequest = new IngestApiDelegateImpl(
                storConfigRepo,
                transService,
                batchDocAsyncService,
                ingestService,
                null,
                clientFieldValidatorUtil
        );

        Optional<NativeWebRequest> result = delegateWithNullRequest.getRequest();
        assertTrue(result.isEmpty());
    }

    @Test
    void testIngest_withSingleStorConfig_success() throws Exception {
        String lobId = "lob";
        String traceId = "trace";

        IngestRequest ingestRequest = mock(IngestRequest.class);

        StorConfig storConfig = mock(StorConfig.class);
        when(storConfigRepo.findByLobId(lobId)).thenReturn(Collections.singletonList(storConfig));

        StorTransaction txn = mock(StorTransaction.class);
        when(txn.getIngestTxnId()).thenReturn("txn123");
        when(ingestService.ingest(eq(lobId), eq(ingestRequest), eq(storConfig))).thenReturn(txn);

        when(clientFieldValidatorUtil.validateClientAppAndExtractPrimaryToken(any(), eq(lobId)))
                .thenReturn("primaryToken");

        ResponseEntity<Ingest200Response> response = delegate.ingest(lobId, traceId, ingestRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("txn123", response.getBody().getIngestTxnId());

        verify(clientFieldValidatorUtil).validateClientAppAndExtractPrimaryToken(any(), eq(lobId));
        verify(storConfigRepo).findByLobId(lobId);
        verify(ingestService).ingest(lobId, ingestRequest, storConfig);
        verify(batchDocAsyncService).triggerAsync(txn, storConfig, traceId);

        verifyNoMoreInteractions(transService);
    }

    @Test
    void testIngest_withNullStorConfigList_throws() {
        String lobId = "lob";
        String traceId = "trace";

        IngestRequest ingestRequest = mock(IngestRequest.class);

        when(storConfigRepo.findByLobId(lobId)).thenReturn(null);
        when(clientFieldValidatorUtil.validateClientAppAndExtractPrimaryToken(any(), eq(lobId)))
                .thenReturn("primaryToken");

        assertThrows(DgvLmServiceException.class, () -> delegate.ingest(lobId, traceId, ingestRequest));

        verify(clientFieldValidatorUtil).validateClientAppAndExtractPrimaryToken(any(), eq(lobId));
        verify(storConfigRepo).findByLobId(lobId);
        verifyNoInteractions(ingestService, batchDocAsyncService, transService);
    }

    @Test
    void testIngest_withEmptyStorConfigList_throws() {
        String lobId = "lob";
        String traceId = "trace";

        IngestRequest ingestRequest = mock(IngestRequest.class);

        when(storConfigRepo.findByLobId(lobId)).thenReturn(Collections.emptyList());
        when(clientFieldValidatorUtil.validateClientAppAndExtractPrimaryToken(any(), eq(lobId)))
                .thenReturn("primaryToken");

        assertThrows(DgvLmServiceException.class, () -> delegate.ingest(lobId, traceId, ingestRequest));

        verify(clientFieldValidatorUtil).validateClientAppAndExtractPrimaryToken(any(), eq(lobId));
        verify(storConfigRepo).findByLobId(lobId);
        verifyNoInteractions(ingestService, batchDocAsyncService, transService);
    }

    @Test
    void testIngest_withMultipleStorConfigs_andRepoMatch_success() throws Exception {
        String lobId = "lob";
        String traceId = "trace";
        String repoId = "repo1";

        // IngestRequest -> Storage -> repoId (avoid needing generated model constructors)
        IngestRequest ingestRequest = mock(IngestRequest.class);
        Object storage = mock(Object.class, invocation -> {
            if ("getRepoId".equals(invocation.getMethod().getName())) return repoId;
            return RETURNS_DEFAULTS.answer(invocation);
        });
        when(ingestRequest.getStorage()).thenReturn((com.td.dgvlm.api.model.IngestStorage) storage);

        StorConfig cfg1 = mock(StorConfig.class);
        StorConfig cfg2 = mock(StorConfig.class);
        when(storConfigRepo.findByLobId(lobId)).thenReturn(List.of(cfg1, cfg2));

        // storageSystem is currently hardcoded "????" in impl, so match anyString()
        when(storConfigRepo.findByLobIdAndStorageSystemAndRepoId(eq(lobId), anyString(), eq(repoId)))
                .thenReturn(Optional.of(cfg2));

        StorTransaction txn = mock(StorTransaction.class);
        when(txn.getIngestTxnId()).thenReturn("txn999");
        when(ingestService.ingest(eq(lobId), eq(ingestRequest), eq(cfg2))).thenReturn(txn);

        when(clientFieldValidatorUtil.validateClientAppAndExtractPrimaryToken(any(), eq(lobId)))
                .thenReturn("primaryToken");

        ResponseEntity<Ingest200Response> response = delegate.ingest(lobId, traceId, ingestRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("txn999", response.getBody().getIngestTxnId());

        verify(storConfigRepo).findByLobId(lobId);
        verify(storConfigRepo).findByLobIdAndStorageSystemAndRepoId(eq(lobId), anyString(), eq(repoId));
        verify(ingestService).ingest(lobId, ingestRequest, cfg2);
        verify(batchDocAsyncService).triggerAsync(txn, cfg2, traceId);
    }

    @Test
    void testIngest_withMultipleStorConfigs_andRepoNoMatch_throws() {
        String lobId = "lob";
        String traceId = "trace";
        String repoId = "repo1";

        IngestRequest ingestRequest = mock(IngestRequest.class);
        Object storage = mock(Object.class, invocation -> {
            if ("getRepoId".equals(invocation.getMethod().getName())) return repoId;
            return RETURNS_DEFAULTS.answer(invocation);
        });
        when(ingestRequest.getStorage()).thenReturn((com.td.dgvlm.api.model.IngestStorage) storage);

        StorConfig cfg1 = mock(StorConfig.class);
        StorConfig cfg2 = mock(StorConfig.class);
        when(storConfigRepo.findByLobId(lobId)).thenReturn(List.of(cfg1, cfg2));

        when(storConfigRepo.findByLobIdAndStorageSystemAndRepoId(eq(lobId), anyString(), eq(repoId)))
                .thenReturn(Optional.empty());

        when(clientFieldValidatorUtil.validateClientAppAndExtractPrimaryToken(any(), eq(lobId)))
                .thenReturn("primaryToken");

        assertThrows(DgvLmServiceException.class, () -> delegate.ingest(lobId, traceId, ingestRequest));

        verify(storConfigRepo).findByLobId(lobId);
        verify(storConfigRepo).findByLobIdAndStorageSystemAndRepoId(eq(lobId), anyString(), eq(repoId));
        verifyNoInteractions(ingestService, batchDocAsyncService, transService);
    }

    @Test
    void testIngestStatus_success() {
        String lobId = "lob";
        String traceId = "trace789";
        String txnId = "txn123";

        IngestTxnRs txnRs = new IngestTxnRs();
        when(transService.getTransactionStatus(txnId)).thenReturn(txnRs);
        when(clientFieldValidatorUtil.validateClientAppAndExtractPrimaryToken(any(), eq(lobId)))
                .thenReturn("primaryToken");

        ResponseEntity<IngestTxnRs> response = delegate.ingestStatus(lobId, traceId, txnId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(txnRs, response.getBody());

        verify(clientFieldValidatorUtil).validateClientAppAndExtractPrimaryToken(any(), eq(lobId));
        verify(transService).getTransactionStatus(txnId);
        verifyNoInteractions(ingestService, batchDocAsyncService, storConfigRepo);
    }
}