if (fileName != null) {
    int dotIndex = fileName.lastIndexOf(".");
    if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
        extension = fileName.substring(dotIndex);
    }
}