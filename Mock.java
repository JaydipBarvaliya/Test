@Mapper(componentModel = "spring")
public interface RejectAttachmentMapper {

    @Mapping(target = "attachmentRequirements", source = "attachmentRequirements")
    RejectAttachment mapToEsl(RejectAttachmentRequest request);

    @Mapping(target = "comment", source = "commentTxt")
    @Mapping(target = "status", constant = "REJECTED")
    @Mapping(target = "id", source = "attachmentId")
    RejectAttachment.AttachmentRequirements map(AttachmentRequirement source);

    // This line is optional, but helps explicitly show list mapping
    List<RejectAttachment.AttachmentRequirements> mapAttachmentRequirements(
        List<AttachmentRequirement> source);
}