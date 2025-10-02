@ResourceLoader
private ResourceLoader resourceLoader;

@Bean
public JCacheCacheManager cacheManager() throws Exception {
    CachingProvider cachingProvider = Caching.getCachingProvider();

    Resource resource = resourceLoader.getResource("classpath:ehcache.xml");
    URI uri = resource.getURI();

    CacheManager cacheManager = cachingProvider.getCacheManager(uri, getClass().getClassLoader());
    return new JCacheCacheManager(cacheManager);
}