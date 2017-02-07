package org.gameontext.sample.jsr107box;

import java.util.Set;
import java.util.logging.Level;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.gameontext.sample.Log;
import org.gameontext.sample.jsr107lock.CacheBasedLock;
import org.gameontext.sample.protocol.SessionSender;

@ApplicationScoped
public class BoxContents {

    @Inject 
    BoxContentsDataBean dataBean;
    
    @Inject 
    CacheBasedLock lock;
    
    @Inject
    SessionSender sender;
    
    /**
     * @param boxName
     * @return the items that are currently in the box.
     */
    public Set<String> getItemsInBox(String boxName){
        return dataBean.getItemsInBox(boxName);
    }
    
    /**
     * @param sourceBox
     * @param destinationBox
     * @param itemName
     * @param connection
     * @return true if the item moved, false otherwise.
     */
    public boolean moveItem(String sourceBox, String destinationBox, String itemName, String userId, String username){
        //get lock on item..
        boolean gotLock = lock.getLock(itemName,userId);
        try{
            if(!gotLock){
                sender.sendResponseToRoom("Someone else appears to be using '"+itemName+"' so it would appear you cannot.", 
                        username+" tries to interact with the "+itemName, userId);
                return false;
            }
            
            String find = null;
            Set<String> sourceItems = dataBean.getItemsInBox(sourceBox);
            for(String i : sourceItems){
                if(i.equals(itemName)){
                    find = i;
                    break;
                }
            }
            if(find==null){
                //item was not in the box, so remove is not going to work.. 
                sender.sendResponseToRoom("It would appear "+itemName+" is not in the "+sourceBox, 
                        null,userId);
                
                return false;
            }else{
                sender.sendResponseToRoom("You remove the "+itemName+" from the "+sourceBox+" and place it into the "+destinationBox, 
                        username+" removes the "+itemName+" from the "+sourceBox+" and places it into the "+destinationBox, userId);
                
                Log.log(Level.INFO, this, "Moving "+itemName+" from "+sourceBox+" to "+destinationBox);
                
                Log.log(Level.INFO, this, "B4: src::"+sourceItems);
                sourceItems.remove(find);
                dataBean.updateItemsInBox(sourceBox, sourceItems);
                Log.log(Level.INFO, this, "AFT: src::"+sourceItems+" via get::"+dataBean.getItemsInBox(sourceBox));
                
                Set<String> destinationItems = getItemsInBox(destinationBox);
                Log.log(Level.INFO, this, "B4: dst::"+destinationItems);
                destinationItems.add(find);
                dataBean.updateItemsInBox(destinationBox, destinationItems);
                Log.log(Level.INFO, this, "AFT: dst::"+destinationItems+" via get::"+dataBean.getItemsInBox(destinationBox));
            }
            return true;
        }finally{
            if(gotLock){
                lock.releaseLock(userId);
            }
        }
    }
    
    public boolean setInitialContent(String boxName, Set<String> items) {
        return items.equals( dataBean.setInitialItemsInBox(boxName, items) );
    }
}
