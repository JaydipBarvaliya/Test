
CREATE SEQUENCE STOR_CONFIG_SEQ
START WITH 1
INCREMENT BY 1
NOCACHE
NOCYCLE;


UPDATE STOR_CONFIG
SET CONFIG_ID = STOR_CONFIG_SEQ.NEXTVAL;


COMMIT;






ALTER TABLE STOR_CONFIG
DROP CONSTRAINT <existing_pk>;

ALTER TABLE STOR_CONFIG
ADD CONSTRAINT PK_STOR_CONFIG
PRIMARY KEY (LOB_ID, STOR_SYS, REPO_ID);



ALTER TABLE STOR_INGEST_TXN
ADD (
    STOR_SYS VARCHAR2(50) NOT NULL,
    REPO_ID VARCHAR2(50) NOT NULL
);

ALTER TABLE STOR_INGEST_TXN
ADD CONSTRAINT FK_TXN_CONFIG
FOREIGN KEY (LOB_ID, STOR_SYS, REPO_ID)
REFERENCES STOR_CONFIG (LOB_ID, STOR_SYS, REPO_ID);




@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorageConfigId implements Serializable {

    @Column(name = "LOB_ID")
    private String lobId;

    @Column(name = "STOR_SYS")
    private String storageSystem;

    @Column(name = "REPO_ID")
    private String repoId;
}




@Entity
@Table(name = "STOR_CONFIG")
@Data
@NoArgsConstructor
public class StorageConfig {

    @EmbeddedId
    private StorageConfigId id;

    @Column(name = "FOLDER_PATH", nullable = false)
    private String folderPath;

    @Column(name = "NAS_HOST")
    private String nasHost;

    @Column(name = "NAS_USER")
    private String nasUser;

    @Column(name = "NAS_PASS")
    private String nasPass;

    @OneToMany(mappedBy = "storageConfig")
    private List<StorageIngestTransaction> transactions;
}



@ManyToOne(fetch = FetchType.LAZY)
@JoinColumns({
    @JoinColumn(name = "LOB_ID", referencedColumnName = "LOB_ID"),
    @JoinColumn(name = "STOR_SYS", referencedColumnName = "STOR_SYS"),
    @JoinColumn(name = "REPO_ID", referencedColumnName = "REPO_ID")
})
private StorageConfig storageConfig;



StorageConfig config = txn.getStorageConfig();




