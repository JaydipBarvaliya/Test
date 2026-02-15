ALTER TABLE STOR_INGEST_TXN 
ADD CONFIG_ID NUMBER;

ALTER TABLE STOR_INGEST_TXN
ADD CONSTRAINT FK_TXN_CONFIG
FOREIGN KEY (CONFIG_ID)
REFERENCES STOR_CONFIG (CONFIG_ID);


@Entity
@Table(name = "STOR_CONFIG",
       uniqueConstraints = {
           @UniqueConstraint(
               name = "UK_STOR_CONFIG_BUSINESS",
               columnNames = {"LOB_ID", "STOR_SYS", "REPO_ID"}
           )
       })
public class StoreConfig {

    @Id
    @Column(name = "CONFIG_ID")
    private Long configId;

    @Column(name = "LOB_ID")
    private String lobId;

    @Column(name = "STOR_SYS")
    private String storSys;

    @Column(name = "REPO_ID")
    private String repoId;

    // optional: reverse mapping
    @OneToMany(mappedBy = "config")
    private List<StorageIngestTransaction> transactions;
}


@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stor_config_seq")
@SequenceGenerator(
    name = "stor_config_seq",
    sequenceName = "STOR_CONFIG_SEQ",
    allocationSize = 1
)
private Long configId;





@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "CONFIG_ID", nullable = false)
private StoreConfig config;


Optional<StoreConfig> findByLobIdAndStorSysAndRepoId(
    String lobId,
    String storSys,
    String repoId
);




