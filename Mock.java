import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

@Service
public class ProjectDiscoveryService {

    public List<ProjectRef> discoverProjects(String rootDir) throws IOException {
        Path root = Paths.get(rootDir).toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("Not a directory: " + root);
        }

        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                .filter(p -> p.getFileName().toString().equalsIgnoreCase("pom.xml"))
                .filter(p -> !p.toString().contains(FileSystems.getDefault().getSeparator() + "target" + FileSystems.getDefault().getSeparator()))
                .map(p -> new ProjectRef(projectNameFromPom(p), p.toString()))
                .sorted(Comparator.comparing(ProjectRef::name))
                .toList();
        }
    }

    private String projectNameFromPom(Path pomPath) {
        // MVP: folder name (later we will parse artifactId)
        Path parent = pomPath.getParent();
        return parent != null ? parent.getFileName().toString() : pomPath.toString();
    }
}
