@Bean
public JCacheCacheManager cacheManager() throws Exception {
    CachingProvider cachingProvider = Caching.getCachingProvider();

    // absolute file path from config project
    Resource resource = resourceLoader.getResource("file:/C:/Codebase/GITHUB/aesig-api-config/ehcache.xml");

    URI uri = resource.getURI();
    CacheManager cacheManager = cachingProvider.getCacheManager(uri, getClass().getClassLoader());
    return new JCacheCacheManager(cacheManager);
}