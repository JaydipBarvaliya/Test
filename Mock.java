@Modifying
@Query("""
  update StorageTransaction t
  set t.status = :status,
      t.state = :state,
      t.lastUpdatedTs = :updatedTs
  where t.dgvlmId = :dgvlmId
""")
void updateTxn(
    @Param("dgvlmId") String dgvlmId,
    @Param("status") TxnStatus status,
    @Param("state") TxnState state,
    @Param("updatedTs") OffsetDateTime updatedTs
);