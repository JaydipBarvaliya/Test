package com.td.esig.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineCacheConfig {

    @Bean
    public CaffeineCacheManager cacheManager() {
        // Create a cache manager for "token"
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("token");

        cacheManager.setCaffeine(
            Caffeine.<String, ResponseEntity<String>>newBuilder()
                .maximumSize(100)
                .expireAfter(new Expiry<String, ResponseEntity<String>>() {
                    @Override
                    public long expireAfterCreate(String key, ResponseEntity<String> value, long currentTime) {
                        try {
                            // Parse expiresAt from JSON body
                            JSONObject json = (JSONObject) new JSONParser().parse(value.getBody());
                            long expiresAt = (Long) json.get("expiresAt");

                            long ttlMillis = expiresAt - System.currentTimeMillis();

                            // Subtract buffer (30s) to avoid expiry-in-flight edge cases
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
                        // Recompute TTL on update
                        return expireAfterCreate(key, value, currentTime);
                    }

                    @Override
                    public long expireAfterRead(String key, ResponseEntity<String> value,
                                                long currentTime, long currentDuration) {
                        // Do not reset expiry when cache entry is read
                        return currentDuration;
                    }
                })
                .removalListener((key, value, cause) ->
                        System.out.printf("Cache removed: key=%s, cause=%s%n", key, cause))
        );

        return cacheManager;
    }
}