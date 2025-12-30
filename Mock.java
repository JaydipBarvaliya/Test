return ResponseEntity.ok(
    vulnerabilityScanService.scan(pom).stream()
        .filter(d -> !d.getVulnerabilities().isEmpty())
        .map(DependencyWithVulns::getDependency)
        .distinct()
        .toList()
);