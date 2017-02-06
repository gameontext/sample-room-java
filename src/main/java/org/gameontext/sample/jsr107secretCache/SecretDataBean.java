package org.gameontext.sample.jsr107secretCache;

import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheResult;
import javax.cache.annotation.CacheValue;

import org.gameontext.sample.jsr107resolverfactories.FiveMinuteExpiringCacheResolverFactory;

@CacheDefaults( cacheName="secrets" , cacheResolverFactory=FiveMinuteExpiringCacheResolverFactory.class)
public class SecretDataBean {

    @CachePut
    public void setSecretForUser(@CacheKey String userid, @CacheValue String secret){
        //no-op
        //by invoking this method, the cache has been updated to hold the value for
        //the user.
    }
    
    @CacheResult
    public String getSecretForUser(String userid){
        //so there really isn't a way to know via annotations 'only' if a cache doesn't have a value.. 
        //but you can rely on that if the cache doesn't have a value, this method will have to 
        //execute, which in turn will cause the cache to store 'null' as the secret, and 
        //we'll just live with that as meaning 'no secret set'.
        return null;
    }
    
}
