package com.td.esig.api.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineCacheConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("token");

        Caffeine<String, ResponseEntity<String>> builder = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfter(new Expiry<String, ResponseEntity<String>>() {
                    @Override
                    public long expireAfterCreate(String key, ResponseEntity<String> value, long currentTime) {
                        return computeTtl(value, currentTime);
                    }

                    @Override
                    public long expireAfterUpdate(String key, ResponseEntity<String> value,
                                                  long currentTime, long currentDuration) {
                        return computeTtl(value, currentTime);
                    }

                    @Override
                    public long expireAfterRead(String key, ResponseEntity<String> value,
                                                long currentTime, long currentDuration) {
                        // Don’t reset TTL on reads
                        return currentDuration;
                    }

                    private long computeTtl(ResponseEntity<String> value, long currentTime) {
                        try {
                            String body = value.getBody();
                            if (body != null) {
                                JsonNode json = objectMapper.readTree(body);
                                JsonNode expNode = json.get("expiresAt");
                                if (expNode != null && expNode.isNumber()) {
                                    long expiresAt = expNode.asLong();
                                    long ttl = expiresAt - currentTime;
                                    // protect against negative/expired values
                                    return (ttl > 0) ? ttl : TimeUnit.SECONDS.toNanos(1);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // fallback: 5 minutes
                        return TimeUnit.MINUTES.toNanos(5);
                    }
                });

        cacheManager.setCaffeine((Caffeine) builder);
        return cacheManager;
    }
}