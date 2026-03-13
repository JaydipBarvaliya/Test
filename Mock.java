Good. This is exactly the point where you should stop doing N database calls and move to a bulk operation. Right now your code is doing something like:

SELECT txn1
UPDATE txn1
SELECT txn2
UPDATE txn2
SELECT txn3
UPDATE txn3

If someone sends 200 IDs, that becomes 400 DB calls. That’s bad design even for an admin API.

The cleaner approach is:

1️⃣ Fetch existing txn IDs in one query
2️⃣ Bulk update FAILURE → ERROR in one query
3️⃣ Calculate success + notFound in memory

So you end up with 2 database queries total.

⸻

1️⃣ Repository Query — Find Existing IDs

Add this method.

@Query(value = """
SELECT ingest_txn_id
FROM stor_ingest_txn
WHERE ingest_txn_id IN (:txnIds)
""", nativeQuery = true)
List<String> findExistingTxnIds(@Param("txnIds") List<String> txnIds);

This returns only the IDs that exist in DB.

⸻

2️⃣ Repository Query — Bulk Update FAILURE → ERROR

Add this:

@Modifying
@Transactional
@Query(value = """
UPDATE stor_ingest_txn
SET status = 'ERROR',
    last_update_dttm = SYSTIMESTAMP
WHERE ingest_txn_id IN (:txnIds)
AND status = 'FAILURE'
""", nativeQuery = true)
int updateStatusToErrorBulk(@Param("txnIds") List<String> txnIds);

Return value = number of rows updated.

⸻

3️⃣ Clean Service Implementation

This replaces your entire loop.

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminIngestService {

    private final StorTxnRepository txnRepo;

    public AdminIngestRs reprocessTransactions(AdminIngestRequest request) {

        List<String> requestedTxnIds = request.getTxnsToReprocess();

        log.info("Admin ingest reprocess requested for {} transactions",
                requestedTxnIds.size());

        if (requestedTxnIds.isEmpty()) {
            AdminIngestRs response = new AdminIngestRs();
            response.setRequestedTxnCount(0);
            response.setSuccessCount(0);
            response.setNotFoundCount(0);
            return response;
        }

        // 1️⃣ Fetch existing IDs
        List<String> existingTxnIds = txnRepo.findExistingTxnIds(requestedTxnIds);

        Set<String> existingSet = new HashSet<>(existingTxnIds);

        // 2️⃣ Find NOT FOUND
        List<String> notFoundIds = requestedTxnIds.stream()
                .filter(id -> !existingSet.contains(id))
                .toList();

        // 3️⃣ Bulk update
        int updatedCount = txnRepo.updateStatusToErrorBulk(existingTxnIds);

        log.info("Bulk update completed. Updated={} NotFound={}",
                updatedCount, notFoundIds.size());

        // 4️⃣ Build response
        AdminIngestRs response = new AdminIngestRs();

        response.setRequestedTxnCount(requestedTxnIds.size());
        response.setSuccessCount(updatedCount);
        response.setNotFoundCount(notFoundIds.size());
        response.setSuccessTxnIds(existingTxnIds);
        response.setNotFoundTxnIds(notFoundIds);

        return response;
    }
}


⸻

4️⃣ DelegateImpl Becomes Clean

Your delegate becomes simple:

@Override
public ResponseEntity<AdminIngestRs> adminIngest(
        String traceabilityID,
        AdminIngestRequest request) {

    AdminIngestRs response = adminIngestService.reprocessTransactions(request);

    return ResponseEntity.ok(response);
}


⸻

5️⃣ DB Calls Comparison

Your Current Code

For 100 transactions:

100 SELECT
100 UPDATE
--------------
200 queries


⸻

Bulk Version

1 SELECT
1 UPDATE
--------------
2 queries

Much better.

⸻

6️⃣ Bonus Improvement (Recommended)

Before running queries, also deduplicate:

List<String> requestedTxnIds =
        request.getTxnsToReprocess().stream().distinct().toList();

Because ops teams sometimes send duplicate IDs.

⸻

7️⃣ Final Repository Summary

Your repository will now have these two new methods:

List<String> findExistingTxnIds(List<String> txnIds);

int updateStatusToErrorBulk(List<String> txnIds);


⸻

8️⃣ Brutally honest advice

For admin APIs, most engineers still write the loop version because it’s easier.

But if this endpoint ever gets called with 500+ IDs, your current approach will start hurting DB performance.

The bulk approach I showed is what good backend teams implement.

⸻

If you want, I can also show you a very slick Oracle trick using RETURNING where you can avoid even the first SELECT query and do everything in one query only. That’s the most optimal solution.