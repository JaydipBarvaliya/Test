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
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("token");
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
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
                    public long expireAfterUpdate(String key, ResponseEntity<String> value, long currentTime, long currentDuration) {
                        return expireAfterCreate(key, value, currentTime);
                    }

                    @Override
                    public long expireAfterRead(String key, ResponseEntity<String> value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
        );
        return cacheManager;
    }
}