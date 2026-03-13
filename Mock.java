@Override
public ResponseEntity<AdminIngestRs> adminIngest(
        String traceabilityID,
        AdminIngestRequest request) {

    AdminIngestRs response = adminIngestService.reprocessTransactions(request);

    return ResponseEntity.ok(response);
}



@Service
@RequiredArgsConstructor
@Slf4j
public class AdminIngestService {

    private final StorTxnRepository txnRepo;

    public AdminIngestRs reprocessTransactions(AdminIngestRequest request) {

        List<String> txnIds = request.getTxnsToReprocess();

        List<String> successIds = new ArrayList<>();
        List<String> notFoundIds = new ArrayList<>();

        log.info("Admin ingest reprocess request received for {} transactions", txnIds.size());

        for (String txnId : txnIds) {

            Optional<StorTransaction> txn = txnRepo.findByIngestTxnId(txnId);

            if (txn.isEmpty()) {
                log.warn("Transaction not found for txnId={}", txnId);
                notFoundIds.add(txnId);
                continue;
            }

            if (txn.get().getStatus().equals(TxnStatus.FAILURE)) {

                txnRepo.updateStatusToError(txnId);

                log.info("Transaction {} moved from FAILURE to ERROR", txnId);

                successIds.add(txnId);
            }
        }

        AdminIngestRs response = new AdminIngestRs();

        response.setRequestedTxnCount(txnIds.size());
        response.setSuccessCount(successIds.size());
        response.setNotFoundCount(notFoundIds.size());
        response.setSuccessTxnIds(successIds);
        response.setNotFoundTxnIds(notFoundIds);

        return response;
    }
}



log.debug("Transactions requested for reprocess: {}",
          LogSanitizeUtil.sanitizeLogObj(txnIds));



