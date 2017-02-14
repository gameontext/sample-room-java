/**
 *  Copyright 2011-2013 Terracotta, Inc.
 *  Copyright 2011-2013 Oracle America Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.gameontext.sample.jsr107resolverfactories;

import java.lang.annotation.Annotation;
import java.util.logging.Level;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.annotation.CacheMethodDetails;
import javax.cache.annotation.CacheResolver;
import javax.cache.annotation.CacheResolverFactory;
import javax.cache.annotation.CacheResult;
import javax.cache.configuration.MutableConfiguration;

import org.gameontext.sample.Log;
import org.gameontext.sample.jsr107defaultprovider.RedissonCacheManagerProvider;
import org.jsr107.ri.annotations.DefaultCacheResolver;

/**
 * A simple implementation of a CacheResolverFactory based heavily on the 
 * implementation of DefaultCacheResolverFactory from the RI annotation modules.
 * <p>
 * Obtains it's cachemanager directly from the service we offer to the RI, so 
 * will have the same cachemanager as used for annotations.
 * <p>
 * Calls abstract method 'getConfig' to obtain a configured config to use 
 * when returning caches from this resolver factory. 
 */
public abstract class AbstractCacheResolverFactory implements CacheResolverFactory{

    CacheManager cacheManager = (new RedissonCacheManagerProvider()).getDefaultCacheManager();
    
    @Override
    public CacheResolver getCacheResolver(CacheMethodDetails<? extends Annotation> cacheMethodDetails) {
        final String cacheName = cacheMethodDetails.getCacheName();
        Cache<?, ?> cache = this.cacheManager.getCache(cacheName);

        if (cache == null) {
          Log.log(Level.WARNING,this,"No Cache named '" + cacheName + "' was found in the CacheManager, a default cache will be created.");
          
          MutableConfiguration<Object, Object> config = getConfig();
          cacheManager.createCache(cacheName, config);
          cache = cacheManager.getCache(cacheName);
        }

        return new DefaultCacheResolver(cache);
    }

    @Override
    public CacheResolver getExceptionCacheResolver(CacheMethodDetails<CacheResult> cacheMethodDetails) {
        final CacheResult cacheResultAnnotation = cacheMethodDetails.getCacheAnnotation();
        final String exceptionCacheName = cacheResultAnnotation.exceptionCacheName();
        if (exceptionCacheName == null || exceptionCacheName.trim().length() == 0) {
          throw new IllegalArgumentException("Can only be called when CacheResult.exceptionCacheName() is specified");
        }

        Cache<?, ?> cache = cacheManager.getCache(exceptionCacheName);

        if (cache == null) {
            Log.log(Level.WARNING,this,"No Cache named '" + exceptionCacheName +
              "' was found in the CacheManager, a default cache will be created.");
          
          MutableConfiguration<Object, Object> config = getConfig();
          cacheManager.createCache(exceptionCacheName, config);
          cache = cacheManager.getCache(exceptionCacheName);
        }

        return new DefaultCacheResolver(cache);
    }
    
    protected abstract MutableConfiguration<Object,Object> getConfig();

}
