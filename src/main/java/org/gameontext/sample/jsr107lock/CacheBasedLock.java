package org.gameontext.sample.jsr107lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.gameontext.sample.Log;

@ApplicationScoped
public class CacheBasedLock {
    
    @Inject
    CacheBasedLockDataBean lockBean;
    
    /** Data store to track locks held by this JVM, in case we need to release them all */
    private Map<String,String> locksHeldByThisJVM = new ConcurrentHashMap<String,String>();
    
    /** Get lock for reference key, for requested userid */
    synchronized public boolean getLock(String reference, String userid){
        Log.log(Level.INFO, this, "Asking for lock on ref:"+reference+" by user "+userid);
        String currentLockedBy = lockBean.getReferenceLockForUserId(reference,userid);
        boolean success = currentLockedBy.equals(userid+lockBean.getUniqueId());
        
        if(success)
            locksHeldByThisJVM.put(reference, userid+lockBean.getUniqueId());
        
        Log.log(Level.INFO, this, "Lock request approved?"+success+" lock deemed to be held by "+currentLockedBy);
        return success;
    }
    
    /** Release lock held by this JVM for reference key */
    synchronized public void releaseLock(String reference){
        lockBean.clearLockForRef(reference);
        locksHeldByThisJVM.remove(reference);
    }
    
    /** Utility method to release all locks we've acquired. */
    synchronized public void releaseAllLocksHeld(){
        for(String reference : locksHeldByThisJVM.keySet()){
            releaseLock(reference);
        }
    }
    

}
