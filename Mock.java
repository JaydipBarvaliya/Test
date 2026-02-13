String fileName = txn.getDgvLFileName();
String extension = ".pdf";

if (fileName != null && fileName.contains(".")) {
    int dotIndex = fileName.lastIndexOf(".");
    if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
        extension = fileName.substring(dotIndex);
    }
}