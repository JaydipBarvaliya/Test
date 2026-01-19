@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionResponse toResponse(StorTxnEntity entity);
}



@Service
public class TransactionStatusService {

    private final StorTxnRepository repository;
    private final TransactionMapper mapper;

    public TransactionStatusService(
            StorTxnRepository repository,
            TransactionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public TransactionResponse getTransactionStatus(String txnId) {
        StorTxnEntity entity = repository.findById(txnId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Transaction not found for txnId: " + txnId
                        )
                );

        return mapper.toResponse(entity);
    }
}


