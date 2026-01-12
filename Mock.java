@Slf4j
@Service
@RequiredArgsConstructor
public class BatchDocServiceImpl implements BatchDocService {

    private final StorTxnRepository storTxnRepository;
    private final RestTemplate restTemplate;

    @Value("${filenet.batchdoc.url}")
    private String batchDocUrl;

    @Async
    @Override
    public void triggerBatchDocAsync(String dgvlmId) {
        try {
            // 1. Fetch STOR_TXN by dgvlmId
            StorTxnEntity txn = storTxnRepository
                    .findByDgvlmId(dgvlmId)
                    .orElseThrow(() -> new IllegalStateException("Transaction not found"));

            // 2. Build BatchDoc request
            BatchDocRequest request = buildBatchDocRequest(txn);

            // 3. Call FileNet BatchDoc API
            ResponseEntity<BatchDocResponse> response =
                    restTemplate.postForEntity(batchDocUrl, request, BatchDocResponse.class);

            // 4. Update DB on success
            if (response.getStatusCode().is2xxSuccessful()) {
                txn.setStorTxnId(response.getBody().getBatchId());
                txn.setStatus("ACTIVE");
                txn.setState("FN_BATCH_TRIGGERED");
                txn.setLastUpdatedTs(Instant.now());
                storTxnRepository.save(txn);
            }

        } catch (Exception ex) {
            log.error("BatchDoc async call failed for dgvlmId={}", dgvlmId, ex);

            storTxnRepository.updateStatusAndState(
                    dgvlmId,
                    "ERROR",
                    "DGVL_PUSHED",
                    Instant.now()
            );
        }
    }
}