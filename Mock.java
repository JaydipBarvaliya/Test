public static String sanitizeForLog(String input) {
    if (input == null) {
        return null;
    }
    return input
        .replace("\n", "_")
        .replace("\r", "_")
        .replace("\t", "_");
}