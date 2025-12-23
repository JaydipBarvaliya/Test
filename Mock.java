ProcessBuilder pb = new ProcessBuilder(
    "cmd.exe", "/c",
    "mvnw.cmd",
    "-s", "C:\\Users\\taf5028\\.m2\\settings.xml",
    "-f", pomPath,
    "dependency:list",
    "-DincludeScope=runtime",
    "-DoutputAbsoluteArtifactFilename=false"
);

pb.directory(new File(System.getProperty("user.dir")));
pb.redirectErrorStream(true);