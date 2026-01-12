private BatchDocRequest buildBatchDocRequest(StorTxnEntity txn) {

    SearchCriteria repoCriteria = new SearchCriteria();
    repoCriteria.setKeyName("Id");
    repoCriteria.setKeyValue(txn.getStorFileId());

    Option outputFileOption = new Option();
    outputFileOption.setKeyName("outputFileName");
    outputFileOption.setKeyValue(txn.getFileName());

    Process process = new Process();
    process.setRepositorySearchCriteria(List.of(repoCriteria));
    process.setOption(List.of(outputFileOption));

    ExtractOption extractOption = new ExtractOption();
    extractOption.setKeyName("outputType");
    extractOption.setKeyValue("txt");

    BatchDocRequest request = new BatchDocRequest();
    request.setPrimaryRepositoryId(txn.getRepoId());
    request.setProcess(List.of(process));
    request.setExtractOption(List.of(extractOption));

    return request;
}