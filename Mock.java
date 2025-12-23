@GetMapping("/projects/{name}/vulnerabilities")
public ResponseEntity<List<DependencyWithVulns>> vulnerabilities(
        @PathVariable String name,
        @RequestParam("pom") String pom
) throws Exception {

    return ResponseEntity.ok(
        vulnerabilityScanService.scan(pom)
    );
}