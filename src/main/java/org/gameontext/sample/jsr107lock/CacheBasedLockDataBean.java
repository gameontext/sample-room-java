package org.gameontext.sample.jsr107lock;

import java.util.UUID;
import java.util.logging.Level;

import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheResult;
import javax.enterprise.context.ApplicationScoped;

import org.gameontext.sample.Log;
import org.gameontext.sample.jsr107resolverfactories.OneMinuteExpiringCacheResolverFactory;

@ApplicationScoped
@CacheDefaults( cacheName="locks" , cacheResolverFactory=OneMinuteExpiringCacheResolverFactory.class)
public class CacheBasedLockDataBean {
    
    //need to differentiate 'this jvm's locks from anyone-elses.
    private String uuid = UUID.randomUUID().toString();
    
    public String getUniqueId(){
        return uuid;
    }
    
    @CacheResult
    public String getReferenceLockForUserId(@CacheKey String item, String userid){
        Log.log(Level.INFO, this, "Cache-Miss, Being invoked to obtain lock on "+item+" for "+userid);
        //if the cache doesn't have an answer for this key, then it's not locked
        //at the mo, so we can return the requested user, which will be cached, 
        //and returned if anyone else asks about it.
        return userid+getUniqueId();
    }
    
    @CacheRemove
    public void clearLockForRef(String item){
        //NO:OP, all the work done by the annotation.
    }
}
