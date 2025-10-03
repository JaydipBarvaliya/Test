package com.td.esig.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
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
        // you can also use new CaffeineCacheManager("token") if you prefer predeclaring
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("token");

        // IMPORTANT: Spring expects Caffeine<Object,Object> here
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfter(new Expiry<Object, Object>() {
                @Override
                public long expireAfterCreate(Object key, Object value, long currentTime) {
                    try {
                        // We only expect ResponseEntity<String> values in this cache
                        if (value instanceof ResponseEntity) {
                            @SuppressWarnings("unchecked")
                            ResponseEntity<String> resp = (ResponseEntity<String>) value;
                            String body = resp.getBody();
                            if (body != null) {
                                JSONObject json = (JSONObject) new JSONParser().parse(body);
                                Object exp = json.get("expiresAt");
                                if (exp != null) {
                                    long expiresAt = (exp instanceof Number)
                                            ? ((Number) exp).longValue()
                                            : Long.parseLong(String.valueOf(exp));
                                    long ttlMillis = expiresAt - System.currentTimeMillis();
                                    // subtract 30s to avoid “expired-in-flight”
                                    ttlMillis = Math.max(ttlMillis - 30_000, 1_000);
                                    return TimeUnit.MILLISECONDS.toNanos(ttlMillis);
                                }
                            }
                        }
                        // Fallback TTL if body/parse/field missing
                        return TimeUnit.MINUTES.toNanos(5);
                    } catch (Exception e) {
                        // Fallback TTL if parsing blows up
                        return TimeUnit.MINUTES.toNanos(5);
                    }
                }

                @Override
                public long expireAfterUpdate(Object key, Object value,
                                              long currentTime, long currentDuration) {
                    // Recompute TTL on update
                    return expireAfterCreate(key, value, currentTime);
                }

                @Override
                public long expireAfterRead(Object key, Object value,
                                            long currentTime, long currentDuration) {
                    // Don’t change expiry on reads
                    return currentDuration;
                }
            })
            .removalListener(new RemovalListener<Object, Object>() {
                @Override
                public void onRemoval(Object key, Object value, RemovalCause cause) {
                    System.out.printf("Cache removed: key=%s, cause=%s%n", key, cause);
                }
            });

        cacheManager.setCaffeine(builder);
        return cacheManager;
    }
}