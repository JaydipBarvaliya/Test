@Entity
@Table(name = "STOR_CONFIG")
public class StorConfigEntity {

    @Id
    @Column(name = "LOB_ID", nullable = false, updatable = false)
    private String lobId;

    @Column(name = "STOR_SYS", nullable = false)
    private String storageSystem;

    @Column(name = "REPO_ID", nullable = false)
    private String repoId;

    @Column(name = "FOLDER_PATH", nullable = false)
    private String folderPath;

    // getters & setters
}