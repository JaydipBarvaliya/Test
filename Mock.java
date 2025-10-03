cacheManager.setCaffeine(
    Caffeine.<String, ResponseEntity<String>>newBuilder()
        .maximumSize(100)
        .expireAfter(new Expiry<String, ResponseEntity<String>>() {
            @Override
            public long expireAfterCreate(String key, ResponseEntity<String> value, long currentTime) {
                try {
                    JSONObject json = (JSONObject) new JSONParser().parse(value.getBody());
                    long expiresAt = (Long) json.get("expiresAt");
                    long ttlMillis = expiresAt - System.currentTimeMillis();
                    ttlMillis = Math.max(ttlMillis - 30_000, 1_000);
                    return TimeUnit.MILLISECONDS.toNanos(ttlMillis);
                } catch (Exception e) {
                    return TimeUnit.MINUTES.toNanos(5);
                }
            }

            @Override
            public long expireAfterUpdate(String key, ResponseEntity<String> value,
                                          long currentTime, long currentDuration) {
                return expireAfterCreate(key, value, currentTime);
            }

            @Override
            public long expireAfterRead(String key, ResponseEntity<String> value,
                                        long currentTime, long currentDuration) {
                return currentDuration;
            }
        })
        .removalListener((String key, ResponseEntity<String> value, RemovalCause cause) ->
            System.out.printf("Cache removed: key=%s, cause=%s%n", key, cause))
);