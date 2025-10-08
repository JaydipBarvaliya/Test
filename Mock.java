package com.td.esig.api.config;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class CaffeineCacheConfigTest {

    private CaffeineCacheConfig config;

    @BeforeEach
    void setup() {
        config = new CaffeineCacheConfig();
        // make sure the cache actually stores entries (avoid maximumSize(0))
        TestUtils.injectField(config, "maximumEntriesInCache", 100);
        TestUtils.injectField(config, "gracePeriodInSeconds", 30);
    }

    private Cache<Object, Object> tokenCache() {
        var mgr = config.cacheManager();
        assertThat(mgr).isNotNull();
        var springCache = mgr.getCache("token");
        assertThat(springCache).isNotNull();
        @SuppressWarnings("unchecked")
        Cache<Object, Object> nativeCache =
            (Cache<Object, Object>) springCache.getNativeCache();
        return nativeCache;
    }

    @Test
    void covers_validJson_path() {
        var cache = tokenCache();
        long expires = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(60);
        var resp = ResponseEntity.ok("{\"expiresAt\":" + expires + "}");
        cache.put("k-valid", resp);          // -> expireAfterCreate (valid JSON, positive TTL, minus grace)
        cache.getIfPresent("k-valid");       // -> expireAfterRead
    }

    @Test
    void covers_invalidJson_fallback_path() {
        var cache = tokenCache();
        cache.put("k-bad-json", ResponseEntity.ok("not-json"));  // -> exception path -> 1s fallback
    }

    @Test
    void covers_nullBody_fallback_path() {
        var cache = tokenCache();
        cache.put("k-null", ResponseEntity.<String>ok().build()); // -> body == null -> 1s fallback
    }

    @Test
    void covers_expiredOrNegativeTtl_path() {
        var cache = tokenCache();
        // TTL becomes <= 0 after subtracting grace (30s)
        long shortExpiry = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(20);
        cache.put("k-expired", ResponseEntity.ok("{\"expiresAt\":" + shortExpiry + "}"));
    }

    @Test
    void covers_update_and_removalListener() {
        var cache = tokenCache();
        long exp1 = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(90);
        cache.put("k-upd", ResponseEntity.ok("{\"expiresAt\":" + exp1 + "}"));

        long exp2 = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(120);
        cache.put("k-upd", ResponseEntity.ok("{\"expiresAt\":" + exp2 + "}")); // -> expireAfterUpdate

        cache.invalidate("k-upd");  // -> triggers removalListener
        assertThat(cache.asMap()).doesNotContainKey("k-upd");
    }
}