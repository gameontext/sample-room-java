package org.gameontext.sample;

import java.util.List;
import java.util.Map;

public interface Item {
    public List<String> getCommandPrefixes();
    public Map<String,String> getCommandHelp();
    public boolean processCommand(String command, String userId, String username);
    public String getName();
    public String getDescription();
}
