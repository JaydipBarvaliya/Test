@RestController
@RequestMapping("/status")
public class TransactionStatusController {

    private final TransactionStatusService service;

    public TransactionStatusController(TransactionStatusService service) {
        this.service = service;
    }

    @GetMapping("/{txnId}")
    public ResponseEntity<TransactionResponse> getTransactionStatus(
            @PathVariable String txnId,
            @RequestHeader("lobId") String lobId,
            @RequestHeader("traceabilityId") String traceabilityId) {

        TransactionResponse response = service.getTransactionStatus(txnId);
        return ResponseEntity.ok(response);
    }
}