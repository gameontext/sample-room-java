package org.gameontext.sample.items;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.gameontext.sample.Item;
import org.gameontext.sample.protocol.SessionSender;

public abstract class AbstractItem implements Item{
    private final String[] commandPrefixes = {"/examine "+getName(), "/look at "+getName()};
    private final Map<String,String> commandHelp = new HashMap<String,String>();
    
    @Inject
    protected SessionSender sender;

    @Override
    public List<String> getCommandPrefixes() {
        return Arrays.asList(commandPrefixes);
    }

    protected AbstractItem(){
        commandHelp.put("/examine","Examines an item");
        commandHelp.put("/look at <item>", "Gives a more detail description of an item");
    }
    
    public Map<String,String> getCommandHelp(){
        return commandHelp;
    }

    @Override
    public boolean processCommand(String command, String userId, String username) {
        if(command.equals("/look at "+getName()) || command.equals("/examine "+getName())){
            return examine(userId, username);
        }
        return false;
    }
    
    private boolean examine(String userId, String username){
        sender.sendResponseToRoom(getDescription(),username+" looks at the "+getName(),userId);
        return true;
    }

    abstract public String getName();

    abstract public String getDescription();

}
