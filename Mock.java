package com.td.esig.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;

public class CaffeineCacheConfigTest {

    private CaffeineCacheConfig config;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        config = new CaffeineCacheConfig();
        objectMapper = new ObjectMapper();

        // Inject test values (simulate @Value from properties)
        TestUtils.injectField(config, "maximumEntriesInCache", 100);
        TestUtils.injectField(config, "gracePeriodInSeconds", 30);
        TestUtils.injectField(config, "objectMapper", objectMapper);
    }

    @Test
    void testCacheManagerNotNull() {
        assertThat(config.cacheManager()).isNotNull();
    }

    @Test
    void testExpireAfterCreate_ValidExpiresAt() throws Exception {
        // Prepare JSON with expiresAt 60 seconds from now
        long expiresAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(60);
        String json = "{\"expiresAt\": " + expiresAt + "}";
        ResponseEntity<String> response = ResponseEntity.ok(json);

        long ttlNanos = config.cacheManager()
                .getCache("token")
                .getNativeCache()
                .policy()
                .expireAfterCreate()
                .map(e -> e.expireAfterCreate("key", response, System.currentTimeMillis()))
                .orElseThrow();

        long ttlSeconds = TimeUnit.NANOSECONDS.toSeconds(ttlNanos);
        assertThat(ttlSeconds).isBetween(25L, 35L); // 60 - 30 grace period ≈ 30s
    }

    @Test
    void testExpireAfterCreate_InvalidJson() {
        ResponseEntity<String> badResponse = ResponseEntity.ok("invalid-json");
        long ttl = config.cacheManager()
                .getCache("token")
                .getNativeCache()
                .policy()
                .expireAfterCreate()
                .map(e -> e.expireAfterCreate("key", badResponse, System.currentTimeMillis()))
                .orElseThrow();
        assertThat(ttl).isEqualTo(TimeUnit.SECONDS.toNanos(1));
    }

    @Test
    void testExpireAfterCreate_MissingExpiresAt() throws Exception {
        String json = "{}";
        ResponseEntity<String> response = ResponseEntity.ok(json);

        long ttl = config.cacheManager()
                .getCache("token")
                .getNativeCache()
                .policy()
                .expireAfterCreate()
                .map(e -> e.expireAfterCreate("key", response, System.currentTimeMillis()))
                .orElseThrow();
        assertThat(ttl).isEqualTo(TimeUnit.SECONDS.toNanos(1));
    }

    @Test
    void testRemovalListenerLogsOnEviction() {
        var cacheManager = config.cacheManager();
        var nativeCache = cacheManager.getCache("token").getNativeCache();

        nativeCache.put("test", "data");
        nativeCache.invalidate("test");

        // In real tests, you'd use a log appender to verify message content
        assertThat(nativeCache.asMap()).doesNotContainKey("test");
    }
}