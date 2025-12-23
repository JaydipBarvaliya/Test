if (line.contains(":jar:") && line.contains(":runtime")) {
    String cleaned = line.replace("[INFO]", "").trim();
    String[] parts = cleaned.split(":");

    if (parts.length >= 5) {
        result.add(new DependencyRef(
            parts[0],
            parts[1],
            parts[3],
            parts[4]
        ));
    }
}