@Service
public class BatchDocAsyncWrapper {

    private final BatchDocService batchDocService;

    public BatchDocAsyncWrapper(BatchDocService batchDocService) {
        this.batchDocService = batchDocService;
    }

    @Async
    public void triggerAsync(
            StorTransaction txn,
            StorConfig storConfig,
            String traceabilityId,
            String primaryToken) {

        batchDocService.triggerBatchDocAPI(
                txn, storConfig, traceabilityId, primaryToken);
    }
}