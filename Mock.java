StorConfig config = txn.getConfig();

String traceabilityId = txn.getTraceabilityId(); // assuming you stored it
String primaryToken = txn.getPrimaryToken();     // assuming you stored it

batchDocService.triggerBatchDocAPIAsync(
        txn,
        config,
        traceabilityId,
        primaryToken
);