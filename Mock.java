@Service
public class IngestService {

    private final StorConfigRepository storageConfigRepo;
    private final StorageTransactionRepository txnRepo;
    private final StorageTransactionMapper mapper;
    private final BatchDocService batchDocService;

    public IngestService(
            StorConfigRepository storageConfigRepo,
            StorageTransactionRepository txnRepo,
            StorageTransactionMapper mapper,
            BatchDocService batchDocService
    ) {
        this.storageConfigRepo = storageConfigRepo;
        this.txnRepo = txnRepo;
        this.mapper = mapper;
        this.batchDocService = batchDocService;
    }

    public String ingest(String lobId, String traceabilityId, IngestRequest request) {

        List<StorageConfigs> configs = storageConfigRepo.findByLobId(lobId);
        if (configs == null || configs.isEmpty()) {
            throw new ForbiddenException("Invalid LOB ID");
        }

        String txnId = UUID.randomUUID().toString();
        OffsetDateTime now = OffsetDateTime.now();

        StorageTransaction txn =
                mapper.toEntity(request, txnId, lobId, now);

        txnRepo.save(txn);

        batchDocService.triggerBatchDocAPIAsync(
                txn,
                configs.get(0),
                lobId,
                traceabilityId
        );

        return txnId;
    }
}