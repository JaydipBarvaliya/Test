@Service
public class VulnerabilityScanService {

    private final DependencyResolverService resolver;
    private final OsvClient osvClient;

    public VulnerabilityScanService(
            DependencyResolverService resolver,
            OsvClient osvClient) {
        this.resolver = resolver;
        this.osvClient = osvClient;
    }

    public List<DependencyWithVulns> scan(String pomPath) throws Exception {

        List<DependencyRef> dependencies =
                resolver.resolveDependencies(pomPath);

        List<DependencyWithVulns> result = new ArrayList<>();

        for (DependencyRef dep : dependencies) {
            List<VulnerabilityRef> vulns = osvClient.scan(dep);

            if (!vulns.isEmpty()) {
                result.add(new DependencyWithVulns(dep, vulns));
            }
        }

        return result;
    }
}