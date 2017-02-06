package org.gameontext.sample.jsr107resolverfactories;

import java.lang.annotation.Annotation;

import javax.cache.annotation.CacheMethodDetails;
import javax.cache.annotation.CacheResolver;
import javax.cache.annotation.CacheResolverFactory;
import javax.cache.annotation.CacheResult;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import org.jsr107.ri.annotations.DefaultCacheResolverFactory;

public class FiveMinuteExpiringCacheResolverFactory implements CacheResolverFactory {
    
    //Create am instance of DefaultCacheResolverFactory configured to customize
    //created caches to have 1 minute expiry from creation.
    private CacheResolverFactory delegate = new DefaultCacheResolverFactory(
            new DefaultCacheResolverFactory.CacheConfigCustomizer() {
                @Override
                public void customizeConfiguration(MutableConfiguration<Object, Object> config) {
                    config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.FIVE_MINUTES));
                }
            });
    
    @Override
    public CacheResolver getCacheResolver(CacheMethodDetails<? extends Annotation> cacheMethodDetails) {
        return delegate.getCacheResolver(cacheMethodDetails);
    }
    
    @Override
    public CacheResolver getExceptionCacheResolver(CacheMethodDetails<CacheResult> cacheMethodDetails) {
        return delegate.getExceptionCacheResolver(cacheMethodDetails);
    }
}
