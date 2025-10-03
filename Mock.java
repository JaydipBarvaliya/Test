package com.td.esig.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineCacheConfig {

    @Bean
    public CaffeineCacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("token");

        // Strongly typed builder: <String, ResponseEntity<String>>
        Caffeine<String, ResponseEntity<String>> builder = Caffeine.newBuilder()
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
                                    long ttl = expiresAt - currentTime;
                                    return (ttl > 0) ? ttl : TimeUnit.SECONDS.toNanos(1); // fallback: 1s if already expired
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // default TTL: 5 minutes
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
                        // don’t reset TTL on reads
                        return currentDuration;
                    }
                });

        // Cast only once when setting into Spring’s cache manager
        cacheManager.setCaffeine((Caffeine) builder);

        return cacheManager;
    }
}