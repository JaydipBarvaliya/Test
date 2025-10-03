.removalListener((String key, ResponseEntity<String> value, RemovalCause cause) -> {
    System.out.printf("Cache removed: key=%s, cause=%s%n", key, cause);
})