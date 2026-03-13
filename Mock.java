Alright. I went through all your screenshots carefully. I’ll give you exact final versions of the two files so you can copy-paste without thinking, and then I’ll give you the final architecture verdict.

No partial snippets. No guessing.

⸻

1️⃣ Final File

AdminApiDelegateImpl.java

package com.td.dgvlm.api.delegate;

import com.td.dgvlm.openapi.api.AdminApiDelegate;
import com.td.dgvlm.openapi.model.AdminIngestRequest;
import com.td.dgvlm.openapi.model.AdminIngestRs;
import com.td.dgvlm.service.AdminIngestService;
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
    public ResponseEntity<AdminIngestRs> adminIngest(
            String traceabilityID,
            AdminIngestRequest adminIngestRequest) {

        log.info(
                "Admin ingest API invoked with {} txnIds. traceabilityId={}",
                adminIngestRequest.getTxnsToReprocess().size(),
                LogSanitizeUtil.sanitizeLogObj(traceabilityID)
        );

        AdminIngestRs response =
                adminIngestService.reprocessTransactions(adminIngestRequest);

        log.info(
                "Admin ingest completed. requested={}, success={}, notFound={}, duplicates={}, otherThanFailure={}, traceabilityId={}",
                response.getRequestedTxnCount(),
                response.getSuccessCount(),
                response.getNotFoundCount(),
                response.getDuplicateCount(),
                response.getOtherThanFailureStatusCount(),
                LogSanitizeUtil.sanitizeLogObj(traceabilityID)
        );

        return ResponseEntity.ok(response);
    }
}


⸻

2️⃣ Final File

AdminIngestService.java

package com.td.dgvlm.service;

import com.td.dgvlm.openapi.model.AdminIngestRequest;
import com.td.dgvlm.openapi.model.AdminIngestRs;
import com.td.dgvlm.repository.StorTxnRepository;
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

        /*
        Handle empty request
         */
        if (inputTxnIds.isEmpty()) {

            AdminIngestRs response = new AdminIngestRs();

            response.setRequestedTxnCount(0);
            response.setSuccessCount(0);
            response.setNotFoundCount(0);
            response.setDuplicateCount(0);
            response.setOtherThanFailureStatusCount(0);

            response.setSuccessTxnIds(Collections.emptyList());
            response.setNotFoundTxnIds(Collections.emptyList());
            response.setDuplicateTxnIds(Collections.emptyList());
            response.setOtherThanFailureStatusTxnIds(Collections.emptyList());

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
        Fetch transactions from DB
         */
        List<String> existingTxnIds = txnRepo.findExistingTxnIds(requestedTxnIds);
        Set<String> existingSet = new HashSet<>(existingTxnIds);

        /*
        NOT FOUND
         */
        List<String> notFoundTxnIds = requestedTxnIds.stream()
                .filter(id -> !existingSet.contains(id))
                .toList();

        /*
        FAILURE status transactions
         */
        List<String> failureTxnIds = txnRepo.findFailureTxnIds(existingTxnIds);

        Set<String> failureSet = new HashSet<>(failureTxnIds);

        /*
        OTHER THAN FAILURE
         */
        List<String> otherThanFailureStatusTxnIds = existingTxnIds.stream()
                .filter(id -> !failureSet.contains(id))
                .toList();

        /*
        Bulk update FAILURE -> ERROR
         */
        int updatedCount = 0;

        if (!failureTxnIds.isEmpty()) {
            updatedCount = txnRepo.updateStatusToErrorBulk(failureTxnIds);
        }

        log.info(
                "Bulk update completed. Updated={}, NotFound={}, Duplicates={}, OtherThanFailure={}",
                updatedCount,
                notFoundTxnIds.size(),
                duplicateTxnIds.size(),
                otherThanFailureStatusTxnIds.size()
        );

        /*
        Build response
         */
        AdminIngestRs response = new AdminIngestRs();

        response.setRequestedTxnCount(inputTxnIds.size());

        response.setSuccessCount(updatedCount);
        response.setNotFoundCount(notFoundTxnIds.size());
        response.setDuplicateCount(duplicateTxnIds.size());
        response.setOtherThanFailureStatusCount(otherThanFailureStatusTxnIds.size());

        response.setSuccessTxnIds(failureTxnIds);
        response.setNotFoundTxnIds(notFoundTxnIds);
        response.setDuplicateTxnIds(duplicateTxnIds);
        response.setOtherThanFailureStatusTxnIds(otherThanFailureStatusTxnIds);

        return response;
    }
}


⸻

3️⃣ REQUIRED Repository Queries

Your service requires these 3 queries.

Query 1 — find existing txn ids

@Query(value = """
SELECT ingest_txn_id
FROM stor_ingest_txn
WHERE ingest_txn_id IN (:txnIds)
""", nativeQuery = true)
List<String> findExistingTxnIds(List<String> txnIds);


⸻

Query 2 — find FAILURE status txns

@Query(value = """
SELECT ingest_txn_id
FROM stor_ingest_txn
WHERE ingest_txn_id IN (:txnIds)
AND status = 'FAILURE'
""", nativeQuery = true)
List<String> findFailureTxnIds(List<String> txnIds);


⸻

Query 3 — bulk update FAILURE → ERROR

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

4️⃣ Example Response (Correct)

Your Swagger example now makes sense:

{
  "requestedTxnCount": 6,
  "successCount": 2,
  "notFoundCount": 3,
  "duplicateCount": 1,
  "otherThanFailureStatusCount": 1,
  "successTxnIds": ["txn1","txn2"],
  "notFoundTxnIds": ["txn3","txn4","txn5"],
  "duplicateTxnIds": ["txn6"],
  "otherThanFailureStatusTxnIds": ["txn7"]
}

Ops team knows exactly what happened.

⸻

5️⃣ Final Architecture Verdict (Important)

This design is actually very good.

Why:

✔ Ops visibility

They know:
	•	duplicates
	•	not found
	•	already processed
	•	successfully reprocessed

Huge operational clarity.

⸻

✔ Bulk processing

Only 3 queries total

1️⃣ find existing
2️⃣ find failure
3️⃣ bulk update

No loops. No N+1 queries.

⸻

✔ Safe

Only updates FAILURE → ERROR

You will never accidentally update SUCCESS or PROCESSING transactions.

⸻

✔ Logging is correct

Logs capture:
	•	request size
	•	success
	•	not found
	•	duplicates
	•	wrong status

Perfect for production debugging.

⸻

6️⃣ The ONLY improvement I would still add

Protect the DB from large requests.

Add validation:

if (inputTxnIds.size() > 500) {
    throw new IllegalArgumentException("Maximum 500 txnIds allowed");
}

Otherwise someone can send 20,000 IDs and kill your DB.

⸻

7️⃣ Final Verdict

Your API now is:

Production-grade ✔
Operationally transparent ✔
Bulk optimized ✔
Safe status handling ✔
Well logged ✔

If this were my code review at a bank, I would approve it.

⸻

If you want, I can also show you one very clever optimization that reduces the queries from 3 → 1 (and makes this API insanely fast).
Most senior backend engineers miss that trick.