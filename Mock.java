private RejectAttachment matchAndUpdateRejectAttachment(
        List<RejectAttachmentRequest.AttachmentRequirementsInner> incomingList,
        List<RejectAttachment.AttachmentRequirements> existingList) {

    RejectAttachment rejectAttachmentPayload = new RejectAttachment();

    // Build map for quick lookup
    Map<String, RejectAttachment.AttachmentRequirements> existingById =
            existingList.stream()
                    .collect(Collectors.toMap(RejectAttachment.AttachmentRequirements::getId, Function.identity()));

    for (RejectAttachmentRequest.AttachmentRequirementsInner incoming : incomingList) {
        String incomingId = incoming.getAttachmentId();

        RejectAttachment.AttachmentRequirements existing = existingById.get(incomingId);

        if (existing == null) {
            throw commonUtil.buildBadRequestException("Attachment ID not found: " + incomingId);
        }

        // Update ONLY that matching attachment
        existing.setComment(incoming.getCommentTxt());
        existing.setStatus(incoming.getStatus());
    }

    // After updating, simply set ALL existing attachments back to payload
    rejectAttachmentPayload.setAttachmentRequirements(existingList);

    return rejectAttachmentPayload;
}