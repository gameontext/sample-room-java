package org.gameontext.sample.jsr107box;

import java.util.Collections;
import java.util.Set;

import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CacheResult;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@CacheDefaults( cacheName = "boxContents" )
public class BoxContentsDataBean {
    
    /**
     * Update box state to proposed set
     * @param boxName
     * @param proposed
     * @return
     */
    @CacheResult( skipGet=true )
    public Set<String> updateItemsInBox(@CacheKey String boxName, Set<String> proposed){
        //because skipGet is set, we'll always invoke this method, and then the cache 
        //will be updated with the response.
        return proposed;
    }
    
    /**
     * Retrieve current items in box
     * @param boxName
     * @return
     */
    @CacheResult
    public Set<String> getItemsInBox(@CacheKey String boxName){
        //this method will only ever execute when the cache has no content for the box
        //this will only happen when the code is first run.
        //so this code essentially seeds the initial state for the boxes.
        //(unless setInitialItemsInBox is called first!)
        
        return Collections.emptySet();
    }
    
    /**
     * Supply initial state for box.. 
     * 
     * @param boxName
     * @param proposed
     */
    @CacheResult
    public Set<String> setInitialItemsInBox(@CacheKey String boxName, Set<String> proposed){
        //this method will only ever execute when the cache has no content for the box
        //this will only happen when the code is first run.
        //so this code essentially seeds the initial state for the boxes.
        return proposed;
    }
    
}
