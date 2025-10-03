long expiresAtMillis = expNode.asLong();   // token expiry as epoch millis
long nowMillis = System.currentTimeMillis(); 
long ttlMillis = expiresAtMillis - nowMillis;

// fallback if parsing failed or expired already
if (ttlMillis <= 0) {
    return TimeUnit.SECONDS.toNanos(1); // expire almost immediately
}

// convert to nanos because Caffeine expects nanoseconds
return TimeUnit.MILLISECONDS.toNanos(ttlMillis);