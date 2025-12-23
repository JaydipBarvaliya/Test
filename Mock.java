new ProcessBuilder(
    "cmd.exe", "/c",
    "mvnw",
    "-f", pomPath,
    "dependency:list",
    "-DincludeScope=runtime",
    "-DoutputAbsoluteArtifactFilename=false"
);