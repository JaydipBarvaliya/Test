// Example Maven output line:
// org.mozilla:rhino:jar:1.7.13:compile -- module rhino (auto)

if (line.contains(":jar:")) {

    String cleaned = line.replace("[INFO]", "").trim();

    // Remove everything after scope (e.g. "-- module xyz")
    cleaned = cleaned.split(" -- ")[0];

    String[] parts = cleaned.split(":");

    if (parts.length >= 5) {
        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[3];
        String scope = parts[4];

        result.add(new DependencyRef(
            groupId,
            artifactId,
            version,
            scope
        ));
    }
}