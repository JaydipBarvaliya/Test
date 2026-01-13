@Repository
public interface StorTxnRepository
        extends JpaRepository<StorageTransaction, UUID> {

    @Modifying
    @Transactional
    @Query("""
        update StorageTransaction t
        set t.status = :status,
            t.state = :state,
            t.lastUpdatedTs = :updatedTs
        where t.dgvlmId = :dgvlmId
    """)
    int updateStatusAndState(
        @Param("dgvlmId") String dgvlmId,
        @Param("status") String status,
        @Param("state") String state,
        @Param("updatedTs") OffsetDateTime updatedTs
    );
}