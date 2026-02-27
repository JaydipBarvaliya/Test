package com.td.dgvlm.api.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchDocService {

    private static final Logger log =
            LoggerFactory.getLogger(BatchDocService.class);

    private final StorTxnRepository txnRepo;
    private final WebClientGateway webClientGateway;
    private final PingFedService pingFedService;

    public void triggerBatchDocAPI(StorTransaction txn,
                                   StorConfig storConfig,
                                   String traceabilityId)
            throws ApiConfigException, ApiException {

        String txnId = txn.getIngestTxnId();
        long startTime = System.currentTimeMillis();

        log.info("BatchDoc API trigger started. txnId={}, repoId={}, folderPath={}",
                txnId,
                storConfig.getRepoId(),
                storConfig.getFolderPath());

        try {

            BatchDocRequest batchdocReqPayload =
                    buildBatchDocRequest(txn, storConfig);

            log.debug("BatchDoc request built for txnId={}", txnId);

            String pingfedToken =
                    pingFedService.getOauth2ClientSecondaryToken();

            log.debug("OAuth token obtained for txnId={}", txnId);

            BatchDocResponse batchdocResp =
                    webClientGateway.callBatchDocAPI(
                            batchdocReqPayload,
                            traceabilityId,
                            pingfedToken
                    );

            log.info("BatchDoc API call successful. txnId={}, batchId={}",
                    txnId,
                    batchdocResp.batchId());

            txn.setStorTxnId(batchdocResp.batchId());
            txn.setStatus(TxnStatus.ACTIVE);
            txn.setState(TxnState.FN_BATCH_TRIGGERED);
            txn.setLastUpdateDttm(OffsetDateTime.now());

            txnRepo.save(txn);

            long duration = System.currentTimeMillis() - startTime;

            log.info("Transaction updated after BatchDoc success. txnId={}, duration={} ms",
                    txnId,
                    duration);

        } catch (Exception ex) {

            log.error("BatchDoc API failed. txnId={}", txnId, ex);

            throw ex; // propagate to caller (retry / async layer handles DB update)
        }
    }

    private BatchDocRequest buildBatchDocRequest(StorTransaction txn,
                                                 StorConfig storConfig) {

        log.debug("Building BatchDoc request for txnId={}, storeFileId={}",
                txn.getIngestTxnId(),
                txn.getStoreFileId());

        BatchDocSearchCriteria repoCriteria =
                new BatchDocSearchCriteria();

        repoCriteria.setKeyName("Id");
        repoCriteria.setKeyValue(txn.getStoreFileId());

        BatchDocOption outputFileBatchDocOption =
                new BatchDocOption();

        outputFileBatchDocOption.setKeyName("outputFileName");

        String extension =
                extractExtension(txn.getStoreFileId());

        outputFileBatchDocOption.setKeyValue(
                txn.getStoreFileId() + extension
        );

        BatchDocProcess process =
                new BatchDocProcess();

        process.setRepositorySearchCriteria(
                List.of(repoCriteria)
        );

        process.setOption(
                List.of(outputFileBatchDocOption)
        );

        log.debug("BatchDoc request prepared successfully for txnId={}",
                txn.getIngestTxnId());

        return new BatchDocRequest(
                storConfig.getRepoId(),
                storConfig.getFolderPath(),
                List.of(process)
        );
    }

    private static String extractExtension(String fileName) {

        String extension = ".pdf";

        if (fileName == null || fileName.isBlank()) {
            log.warn("File name is null or empty. Defaulting extension to .pdf");
            return extension;
        }

        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            extension = fileName.substring(dotIndex);
            log.debug("Extracted extension '{}' from fileName={}",
                    extension,
                    fileName);
        } else {
            log.debug("No extension found in fileName={}. Defaulting to .pdf",
                    fileName);
        }

        return extension;
    }
}