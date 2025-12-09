private UpdateAttachmentStatus matchAndUpdateRejectAttachment(
        List<UpdateAttachmentStatusRequest.AttachmentRequirementsInner> incomingList,
        List<UpdateAttachmentStatus.AttachmentRequirements> existingList) 
        throws SharedServiceLayerException {

    // Step 1: Detect duplicate IDs
    Set<String> seen = new HashSet<>();
    for (UpdateAttachmentStatusRequest.AttachmentRequirementsInner item : incomingList) {
        String id = item.getAttachmentId();
        if (!seen.add(id)) {
            throw commonUtil.buildBadRequestException("Duplicate attachment ID detected: " + id);
        }
    }

    UpdateAttachmentStatus updateAttachmentStatusPayload = new UpdateAttachmentStatus();

    // Build map for fast lookup
    Map<String, UpdateAttachmentStatus.AttachmentRequirements> existingById =
            existingList.stream()
                    .collect(Collectors.toMap(
                            UpdateAttachmentStatus.AttachmentRequirements::getId,
                            Function.identity()));

    // Apply updates
    for (UpdateAttachmentStatusRequest.AttachmentRequirementsInner incoming : incomingList) {
        String incomingId = incoming.getAttachmentId();
        UpdateAttachmentStatus.AttachmentRequirements existing = existingById.get(incomingId);

        if (existing == null) {
            throw commonUtil.buildBadRequestException("Attachment ID not found: " + incomingId);
        }

        existing.setComment(incoming.getCommentTxt());
        existing.setStatus(incoming.getStatus());
    }

    updateAttachmentStatusPayload.setAttachmentRequirements(existingList);
    return updateAttachmentStatusPayload;
}