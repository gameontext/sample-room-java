package org.gameontext.sample.items.boxes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.gameontext.sample.Item;
import org.gameontext.sample.jsr107box.BoxContents;
import org.gameontext.sample.protocol.SessionSender;


public abstract class AbstractBox implements Item {
    
    private final String[] commandPrefixes = {"/examine "+getName(), "/take", "/put", "/look at "+getName(), "/look in "+getName()};  
    private final Map<String,String> commandHelp = new HashMap<String,String>();
    
    @Inject
    BoxContents boxContents;
    
    @Inject
    SessionSender sender;
    
    protected AbstractBox(){
        commandHelp.put("/examine","Examines an item");
        commandHelp.put("/take <item> from <box>", "Takes an item from a box and places it in the room.");
        commandHelp.put("/put <item> in <box>", "Takes an item from the room and places it in a box.");
        commandHelp.put("/look in <box>", "Tells you what items are in a box");
    }
    
    public Map<String,String> getCommandHelp(){
        return commandHelp;
    }
    
    public List<String> getCommandPrefixes(){
        return Arrays.asList(commandPrefixes);
    }
    
    public boolean processCommand(String command, String userId, String username){
        if(command.equals("/look at "+getName()) || command.equals("/examine "+getName())){
            return examine(userId, username);
        }else if(command.equals("/look in "+getName())){
            return contents(userId, username);
        }else if(command.startsWith("/take ") && command.endsWith(" from "+getName())){
            return removeItem(userId, username, command);
        }else if(command.startsWith("/put ") && command.endsWith(" in "+getName())){
            return addItem(userId, username, command);
        }else{
            return false;
        }
    }
    
    private boolean examine(String userId, String username){
        sender.sendResponseToRoom(getDescription(),username+" looks at the "+getName(),userId);
        return true;
    }

    private boolean contents(String userId, String username){
        
        String items = "";        
        for(String i : boxContents.getItemsInBox(getName())){
            items+=","+i;
        }
        if(items.length()>0){
            items="["+items.substring(1);
            items+="]";
    
            sender.sendResponseToRoom("The box appears to contain "+items,username+" looks at the "+getName(), userId);
        }else{
            sender.sendResponseToRoom("The box appears to be empty", username+" looks at the "+getName(), userId);
        }
        return true;
    }
    
    protected boolean setInitialContents(Set<String> items){
        return boxContents.setInitialContent(getName(), items);
    }
    
    private boolean removeItem(String userId, String username, String command){
        //extract the item name.. 
        String itemName = command.substring("/take ".length(),command.length()-(" from "+getName()).length() ).trim();
        
        boxContents.moveItem(getName(), "room", itemName, userId, username);
        
        //we return true when we handled the command, regardless of if it succeeded.
        return true;
    }
    
    private boolean addItem(String userId, String username, String command){
        //extract the item name.. 
        String itemName = command.substring("/put ".length(),command.length()-(" in "+getName()).length() );
        
        boxContents.moveItem("room", getName(), itemName, userId, username);
        
        return true;
    }
    
    public Set<String> getContents(){
        return boxContents.getItemsInBox(getName());
    }
    

}
