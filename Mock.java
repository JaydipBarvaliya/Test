@Slf4j
public class CacheEventLogger implements CacheEventListener<Object, Object> {

    @Override
    public void onEvent(CacheEvent<? extends Object, ? extends Object> cacheEvent) {
        if (log.isDebugEnabled()) {
            String maskedNewValue = cacheEvent.getNewValue() != null 
                    ? cacheEvent.getNewValue().toString().replaceAll("(?<=.{4}).", "*") 
                    : null;

            log.debug("Cache event [{}]: key={}, oldValue={}, newValue={}", 
                      cacheEvent.getType(), cacheEvent.getKey(), 
                      cacheEvent.getOldValue(), maskedNewValue);
        }
    }
}