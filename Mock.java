@Bean
public CaffeineCacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("token");
    cacheManager.setCaffeine(
        Caffeine.<String, ResponseEntity<String>>newBuilder()
            .maximumSize(100)
            .expireAfter(new Expiry<String, ResponseEntity<String>>() {
                @Override
                public long expireAfterCreate(String key, ResponseEntity<String> value, long currentTime) {
                    try {
                        String body = value.getBody();
                        if (body != null) {
                            JSONObject json = (JSONObject) new JSONParser().parse(body);
                            Object exp = json.get("expiresAt");
                            if (exp instanceof Number) {
                                long expiresAt = ((Number) exp).longValue();
                                return expiresAt - currentTime;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // fallback: 5 minutes
                    return TimeUnit.MINUTES.toNanos(5);
                }

                @Override
                public long expireAfterUpdate(String key, ResponseEntity<String> value,
                                              long currentTime, long currentDuration) {
                    return expireAfterCreate(key, value, currentTime);
                }

                @Override
                public long expireAfterRead(String key, ResponseEntity<String> value,
                                            long currentTime, long currentDuration) {
                    return currentDuration; // don’t reset TTL on reads
                }
            })
    );
    return cacheManager;
}