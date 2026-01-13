int updated = storTxnRepository.updateStatusAndState(
    dgvlmId,
    "ERROR",
    "DGVM_PUSHED",
    OffsetDateTime.now(ZoneOffset.UTC)
);

if (updated == 0) {
    log.warn("No transaction updated for dgvlmId={}", dgvlmId);
}