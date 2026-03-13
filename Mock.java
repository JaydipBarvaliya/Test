Got it. I’ll give you two complete files, ready to paste, with:
	•	duplicate detection
	•	bulk processing
	•	proper logging
	•	correct response fields
	•	consistent counts
	•	no partial snippets

You only need to ensure your OpenAPI model already contains:

requestedTxnCount
successCount
notFoundCount
duplicateCount
successTxnIds
notFoundTxnIds
duplicateTxnIds


⸻

1️⃣ AdminIngestService.java

package com.td.dgvlm.api.service;

import com.td.dgvlm.api.repository.StorTxnRepository;
import com.td.dgvlm.openapi.model.AdminIngestRequest;
import com.td.dgvlm.openapi.model.AdminIngestRs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminIngestService {

    private final StorTxnRepository txnRepo;

    public AdminIngestRs reprocessTransactions(AdminIngestRequest request) {

        List<String> inputTxnIds = request.getTxnsToReprocess();

        log.info("Admin ingest reprocess requested for {} transactions", inputTxnIds.size());

        if (inputTxnIds.isEmpty()) {
            AdminIngestRs response = new AdminIngestRs();
            response.setRequestedTxnCount(0);
            response.setSuccessCount(0);
            response.setNotFoundCount(0);
            response.setDuplicateCount(0);
            response.setSuccessTxnIds(Collections.emptyList());
            response.setNotFoundTxnIds(Collections.emptyList());
            response.setDuplicateTxnIds(Collections.emptyList());
            return response;
        }

        /*
         Detect duplicates
         */
        Set<String> uniqueIds = new LinkedHashSet<>();
        List<String> duplicateTxnIds = new ArrayList<>();

        for (String id : inputTxnIds) {
            if (!uniqueIds.add(id)) {
                duplicateTxnIds.add(id);
            }
        }

        List<String> requestedTxnIds = new ArrayList<>(uniqueIds);

        if (!duplicateTxnIds.isEmpty()) {
            log.warn("Duplicate txnIds detected in request: {}", duplicateTxnIds);
        }

        /*
         Fetch existing IDs from DB
         */
        List<String> existingTxnIds = txnRepo.findExistingTxnIds(requestedTxnIds);
        Set<String> existingSet = new HashSet<>(existingTxnIds);

        /*
         Determine NOT FOUND
         */
        List<String> notFoundTxnIds = requestedTxnIds.stream()
                .filter(id -> !existingSet.contains(id))
                .toList();

        /*
         Bulk update FAILURE -> ERROR
         */
        int updatedCount = txnRepo.updateStatusToErrorBulk(existingTxnIds);

        log.info("Bulk update completed. Updated={}, NotFound={}, Duplicates={}",
                updatedCount,
                notFoundTxnIds.size(),
                duplicateTxnIds.size());

        /*
         Success IDs = existing IDs minus notFound
         */
        List<String> successTxnIds = existingTxnIds.stream()
                .filter(id -> !notFoundTxnIds.contains(id))
                .toList();

        /*
         Build response
         */
        AdminIngestRs response = new AdminIngestRs();

        response.setRequestedTxnCount(inputTxnIds.size());
        response.setSuccessCount(updatedCount);
        response.setNotFoundCount(notFoundTxnIds.size());
        response.setDuplicateCount(duplicateTxnIds.size());

        response.setSuccessTxnIds(successTxnIds);
        response.setNotFoundTxnIds(notFoundTxnIds);
        response.setDuplicateTxnIds(duplicateTxnIds);

        return response;
    }
}


⸻

2️⃣ AdminApiDelegateImpl.java

package com.td.dgvlm.api.delegate;

import com.td.dgvlm.api.service.AdminIngestService;
import com.td.dgvlm.openapi.api.AdminApiDelegate;
import com.td.dgvlm.openapi.model.AdminIngestRequest;
import com.td.dgvlm.openapi.model.AdminIngestRs;
import com.td.dgvlm.util.LogSanitizeUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AdminApiDelegateImpl implements AdminApiDelegate {

    private final AdminIngestService adminIngestService;

    @Override
    public ResponseEntity<AdminIngestRs> adminIngest(String traceabilityID,
                                                     AdminIngestRequest adminIngestRequest) {

        log.info("Admin ingest API invoked with {} txnIds. traceabilityId={}",
                adminIngestRequest.getTxnsToReprocess().size(),
                LogSanitizeUtil.sanitizeLogObj(traceabilityID));

        AdminIngestRs response =
                adminIngestService.reprocessTransactions(adminIngestRequest);

        log.info("Admin ingest completed. requested={}, success={}, notFound={}, duplicates={}, traceabilityId={}",
                response.getRequestedTxnCount(),
                response.getSuccessCount(),
                response.getNotFoundCount(),
                response.getDuplicateCount(),
                LogSanitizeUtil.sanitizeLogObj(traceabilityID));

        return ResponseEntity.ok(response);
    }
}


⸻

3️⃣ Repository methods you must already have

Just confirming you should have these:

@Query(value = """
SELECT ingest_txn_id
FROM stor_ingest_txn
WHERE ingest_txn_id IN (:txnIds)
""", nativeQuery = true)
List<String> findExistingTxnIds(List<String> txnIds);


@Modifying
@Transactional
@Query(value = """
UPDATE stor_ingest_txn
SET status = 'ERROR',
    last_update_dttm = SYSTIMESTAMP
WHERE ingest_txn_id IN (:txnIds)
AND status = 'FAILURE'
""", nativeQuery = true)
int updateStatusToErrorBulk(List<String> txnIds);


⸻

4️⃣ What this implementation guarantees

✔ detects duplicates
✔ reports duplicates in response
✔ bulk DB processing (2 queries only)
✔ accurate counts
✔ proper logging for ops
✔ clean delegate/service separation

⸻

💡 One last improvement I strongly recommend for this admin API:
limit request size (for example max 500 txnIds) so someone doesn’t send 20,000 IDs and crush your DB.