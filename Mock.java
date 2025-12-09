// Step 1: Detect duplicate IDs in incomingList
List<String> incomingIds = incomingList.stream()
        .map(UpdateAttachmentStatusRequest.AttachmentRequirementsInner::getAttachmentId)
        .collect(Collectors.toList());

Set<String> uniqueIds = new HashSet<>();

for (String id : incomingIds) {
    if (!uniqueIds.add(id)) {
        throw commonUtil.buildBadRequestException("Duplicate attachment ID detected: " + id);
    }
}