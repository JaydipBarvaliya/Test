@Override
public long expireAfterCreate(Object key, Object value, long currentTime) {

    // value is String (JWT)
    String token = (String) value;

    DecodedJWT jwt = JWT.decode(token);

    long expSeconds = jwt.getExpiresAt().getTime(); // millis
    long nowMillis = System.currentTimeMillis();

    long ttlMillis = expSeconds - nowMillis;

    // subtract safety buffer
    ttlMillis -= TimeUnit.SECONDS.toMillis(gracePeriodInSeconds);

    if (ttlMillis <= 0) {
        return 0L;
    }

    return TimeUnit.MILLISECONDS.toNanos(ttlMillis);
}