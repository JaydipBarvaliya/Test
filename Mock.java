public record VulnerabilityRef(
    String cveId,
    String summary,
    double cvss,
    String severity,
    String reference
) {}



public record DependencyWithVulns(
    DependencyRef dependency,
    List<VulnerabilityRef> vulnerabilities
) {}



@Service
public class OsvClient {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<VulnerabilityRef> scan(DependencyRef dep) throws Exception {

        String body = """
        {
          "package": {
            "ecosystem": "Maven",
            "name": "%s:%s"
          },
          "version": "%s"
        }
        """.formatted(dep.groupId(), dep.artifactId(), dep.version());

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.osv.dev/v1/query"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(response.body());
        if (!root.has("vulns")) return List.of();

        List<VulnerabilityRef> vulns = new ArrayList<>();
        for (JsonNode v : root.get("vulns")) {
            vulns.add(new VulnerabilityRef(
                v.path("id").asText(),
                v.path("summary").asText(),
                v.path("severity").path(0).path("score").asDouble(0.0),
                v.path("severity").isEmpty() ? "UNKNOWN" : "KNOWN",
                v.path("references").path(0).path("url").asText("")
            ));
        }
        return vulns;
    }
}




GET /api/projects/{name}/vulnerabilities


    GET /api/projects/aesig-api/vulnerabilities?pom=C:/Codebase/GITHUB/aesig-api/pom.xml

    


    
