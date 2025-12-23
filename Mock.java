public record DependencyRef(
    String groupId,
    String artifactId,
    String version,
    String scope
) {}




import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class DependencyResolverService {

    public List<DependencyRef> resolveDependencies(String pomPath)
            throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(
            "mvn",
            "-f", pomPath,
            "dependency:list",
            "-DincludeScope=runtime",
            "-DoutputAbsoluteArtifactFilename=false"
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        List<DependencyRef> result = new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Example line:
                // org.mozilla:rhino:jar:1.7.13:runtime
                if (line.startsWith("   ") && line.contains(":")) {
                    String[] parts = line.trim().split(":");
                    if (parts.length >= 5) {
                        result.add(new DependencyRef(
                            parts[0],
                            parts[1],
                            parts[3],
                            parts[4]
                        ));
                    }
                }
            }
        }

        process.waitFor();
        return result;
    }
}



@GetMapping("/projects/{name}/dependencies")
public ResponseEntity<List<DependencyRef>> dependencies(
        @PathVariable String name,
        @RequestParam("pom") String pom)
        throws Exception {

    return ResponseEntity.ok(
        resolver.resolveDependencies(pom)
    );
}





GET http://localhost:8080/api/projects/aesig-api/dependencies?pom=C:/Codebase/GITHUB/aesig-api/pom.xml


  



