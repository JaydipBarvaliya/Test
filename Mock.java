import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineCacheConfig {

    @Value("${cache.caffeine.maximum-size:100}") // 👈 reads from application.properties
    private int maximumSize; // default = 100

    @Bean
    public CaffeineCacheManager cacheManager() {

        CaffeineCacheManager cacheManager = new CaffeineCacheManager("token");

        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .maximumSize(maximumSize)  // 👈 directly injected here
                .expireAfter(new Expiry<Object, Object>() {
                    @Override
                    public long expireAfterCreate(Object key, Object value, long currentTime) {
                        return TimeUnit.MINUTES.toNanos(5); // whatever logic you have
                    }

                    @Override
                    public long expireAfterUpdate(Object key, Object value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }

                    @Override
                    public long expireAfterRead(Object key, Object value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                });

        cacheManager.setCaffeine(builder);
        return cacheManager;
    }
}