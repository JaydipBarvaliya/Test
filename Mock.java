@Bean
public CaffeineCacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("token");
    cacheManager.setCaffeine(
        Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfter(new Expiry<String, TokenResponse>() {
                    @Override
                    public long expireAfterCreate(String key, TokenResponse value, long currentTime) {
                        // Use token's expiresAt minus safety buffer (e.g., 30 seconds)
                        long ttlMillis = value.getExpiresAt() - System.currentTimeMillis() - 30_000;
                        return TimeUnit.MILLISECONDS.toNanos(Math.max(ttlMillis, 1000));
                    }

                    @Override
                    public long expireAfterUpdate(String key, TokenResponse value, long currentTime, long currentDuration) {
                        return expireAfterCreate(key, value, currentTime);
                    }

                    @Override
                    public long expireAfterRead(String key, TokenResponse value, long currentTime, long currentDuration) {
                        return currentDuration; // don’t reset on reads
                    }
                })
    );
    return cacheManager;
}