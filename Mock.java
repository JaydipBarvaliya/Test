Caffeine.<String, ResponseEntity<String>>newBuilder()
    .maximumSize(100)
    .expireAfter(new Expiry<String, ResponseEntity<String>>() {
        @Override
        public long expireAfterCreate(String key, ResponseEntity<String> value, long currentTime) {
            try {
                JSONObject json = (JSONObject) new JSONParser().parse(value.getBody());
                long expiresAt = (Long) json.get("expiresAt");
                long ttlMillis = expiresAt - System.currentTimeMillis();
                ttlMillis = Math.max(ttlMillis - 30_000, 1_000); // subtract 30s buffer
                return TimeUnit.MILLISECONDS.toNanos(ttlMillis);
            } catch (Exception e) {
                return TimeUnit.MINUTES.toNanos(5); // fallback
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
            return currentDuration; // don’t reset expiry on read
        }
    });