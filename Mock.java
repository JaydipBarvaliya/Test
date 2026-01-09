keytool -list -v \
  -keystore esignature-esig-api.dev.td.com.p12 \
  -storetype PKCS12




@Entity
@Table(name = "STOR_TXN")
public class StorTxnEntity {

    @Id
    @Column(name = "DGVLM_ID", nullable = false, updatable = false)
    private UUID dgvlmId;

    @Column(name = "LOB_ID", nullable = false)
    private String lobId;

    @Column(name = "DRAWER_ID", nullable = false)
    private String drawerId;

    @Column(name = "FOLDER_ID", nullable = false)
    private String folderId;

    @Column(name = "FILE_ID", nullable = false)
    private String storFileId;

    @Column(name = "FILENAME", nullable = false)
    private String fileName;

    @Column(name = "STOR_TXN_ID")
    private String storTxnId;

    @Column(name = "STATUS", nullable = false)
    private String status;

    @Column(name = "STATE", nullable = false)
    private String state;

    @Column(name = "CREATED_TS", nullable = false)
    private OffsetDateTime createdTs;

    @Column(name = "LAST_UPDATED_TS", nullable = false)
    private OffsetDateTime lastUpdatedTs;

    // getters & setters
}