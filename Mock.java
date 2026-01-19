@Mapper(componentModel = "spring")
public interface StorageTransactionMapper {

    @Mapping(target = "txnId", source = "txnId")
    @Mapping(target = "lobId", source = "lobId")
    @Mapping(target = "drawerId", source = "request.drawerId")
    @Mapping(target = "folderId", source = "request.folderId")
    @Mapping(target = "fileName", source = "request.fileName")
    @Mapping(target = "fileId", source = "request.fileId")
    @Mapping(target = "storeFileId", source = "request.storFileId")
    @Mapping(target = "storTxnId", ignore = true)
    @Mapping(target = "state", constant = "RECEIVED")
    @Mapping(target = "status", constant = "NEW")
    @Mapping(target = "createdTs", source = "now")
    @Mapping(target = "lastUpdatedTs", source = "now")
    StorageTransaction toEntity(
            IngestRequest request,
            String txnId,
            String lobId,
            OffsetDateTime now
    );
}