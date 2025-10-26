List<RejectAttachment.AttachmentRequirements> updatedAttachments = new ArrayList<>();

List<RejectAttachmentRequestAttachmentRequirementsInner> incomingList =
        rejectAttachmentRequest.getAttachmentRequirements();
List<RejectAttachment.AttachmentRequirements> existingList = listOfExistingAttachment;

// Build a map for fast lookup by ID
Map<String, RejectAttachment.AttachmentRequirements> existingById = existingList.stream()
        .collect(Collectors.toMap(RejectAttachment.AttachmentRequirements::getId, Function.identity()));

for (RejectAttachmentRequestAttachmentRequirementsInner incoming : incomingList) {
    String incomingId = incoming.getAttachmentId();
    RejectAttachment.AttachmentRequirements existing = existingById.get(incomingId);

    // If not found → throw error
    if (existing == null) {
        throw new IllegalArgumentException("Attachment ID not found: " + incomingId);
    }

    // ✅ Directly replace fields
    existing.setComment(incoming.getCommentTxt());
    existing.setStatus(incoming.getStatus());

    // Add updated record
    updatedAttachments.add(existing);
}

// Replace the old list with updated one
listOfExistingAttachment.clear();
listOfExistingAttachment.addAll(updatedAttachments);