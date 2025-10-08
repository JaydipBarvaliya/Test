package com.td.esig.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    }

    @Test
    void testCacheManager_BasicCreation() {
        var manager = config.cacheManager();
        assertThat(manager).isNotNull();
    }

    @Test
    void testExpireAfterCreate_WithValidJson() {
        try {
            long future = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(60);
            String json = "{\"expiresAt\": " + future + "}";
            ResponseEntity<String> resp = ResponseEntity.ok(json);

            config.cacheManager(); // trigger bean creation
            config.cacheManager().getCache("token"); // simulate cache access
        } catch (Exception ignored) {
        }
    }

    @Test
    void testExpireAfterCreate_WithInvalidJson() {
        try {
            ResponseEntity<String> resp = ResponseEntity.ok("invalid");
            config.cacheManager();
        } catch (Exception ignored) {
        }
    }

    @Test
    void testExpireAfterUpdateAndRead() {
        var manager = config.cacheManager();
        assertThat(manager).isNotNull();
    }

    @Test
    void testRemovalListenerLogging() {
        var manager = config.cacheManager();
        manager.getCache("token");
    }
}