@Bean
public CaffeineCacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("token");

    cacheManager.setCaffeine(
        Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfter(new Expiry<String, ResponseEntity<String>>() {
                    @Override
                    public long expireAfterCreate(String key, ResponseEntity<String> value, long currentTime) {
                        try {
                            // Parse expiresAt from response body
                            JSONObject json = (JSONObject) new JSONParser().parse(value.getBody());
                            long expiresAt = (Long) json.get("expiresAt");
                            long ttlMillis = expiresAt - System.currentTimeMillis();

                            // Add safety buffer (e.g. 30s less)
                            ttlMillis = Math.max(ttlMillis - 30_000, 1_000);

                            return TimeUnit.MILLISECONDS.toNanos(ttlMillis);
                        } catch (Exception e) {
                            // Fallback TTL if parsing fails
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
                        return currentDuration; // don’t reset on read
                    }
                })
                .removalListener((key, value, cause) ->
                        System.out.printf("Cache expired: key=%s, cause=%s%n", key, cause))
    );

    return cacheManager;
}