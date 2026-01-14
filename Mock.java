public class BatchDocRequest {

    private String repositoryId;
    private String folderPath;
    private Process process;

    public static class Process {
        private SearchCriteria searchCriteria;
        private List<Option> options;
    }

    public static class SearchCriteria {
        private String keyName;
        private String keyValue;
    }

    public static class Option {
        private String keyName;
        private String keyValue;
    }
}