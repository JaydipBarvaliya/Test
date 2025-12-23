import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ScanController {

    private final ProjectDiscoveryService discovery;

    public ScanController(ProjectDiscoveryService discovery) {
        this.discovery = discovery;
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectRef>> projects(@RequestParam("root") String root) throws IOException {
        return ResponseEntity.ok(discovery.discoverProjects(root));
    }
}
