/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.gameontext.sample;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.gameontext.sample.items.Items;
import org.gameontext.sample.map.client.MapData;

/**
 * This is how our room is described.
 *  a) Use post-construct in {@link RoomImplementation} to fill some of this in by asking the map
 *  b) Update attributes dynamically in {@link RoomImplementation} as the room is used
 *
 *  @see RoomImplementation
 */
public class RoomDescription {

    private final JsonObject EMPTY_COMMANDS = Json.createObjectBuilder().build();
    private final JsonArray EMPTY_INVENTORY = Json.createArrayBuilder().build();

    private String name = "nickName";
    private String fullName = "A room with no full name";
    private String description = "An undescribed room (or perhaps the data hasn't been fetched from the map)";

    private Map<String, String> commands = new ConcurrentHashMap<>();

    //required else this bean cannot be injected to elsewhere
    public RoomDescription() {}
    
    @Inject
    Items items;

    /**
     * Create a new room description based on data retrieved from the Map service
     * @param data Map data
     */
    public RoomDescription(MapData data) {
        updateData(data);
    }

    /**
     * Update the room description based on data retrieved from the Map service
     * @param data Map data
     */
    public void updateData(MapData data) {
        if ( data.getName() != null ) {
            this.name = data.getName();
        }

        if ( data.getFullName() != null ) {
            this.fullName = data.getFullName();
        }

        if ( data.getDescription() != null ) {
            this.description = data.getDescription();
        }
    }

    /**
     * @return The room's short name
     */
    public String getName() {
        return name;
    }

    /**
     * @return The room's long name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * The display name for a room can change at any time.
     * @param fullName A new display name for the room
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Custom commands are optional. Build/cache/return a JsonObject listing
     * commands and a description of what they do for use in location messages
     * @return JsonObject containing custom room commands. Never null.
     */
    public JsonObject getCommands() {
        JsonObject obj = null;

        if ( commands.isEmpty()) {
            return EMPTY_COMMANDS;
        } else if ( obj == null) {
            JsonObjectBuilder newCommandObj = Json.createObjectBuilder();
            Map<String,String> commandHelp = new HashMap<>();
            
            //merge the default commands with any commands coming from items in the room.
            commandHelp.putAll(commands);
            items.getItemsInRoom().forEach(i -> i.getCommandHelp().entrySet().forEach( c -> commandHelp.put( c.getKey(), c.getValue())));

            //create json response
            commandHelp.entrySet().forEach(e -> { newCommandObj.add(e.getKey(), e.getValue()); });
            obj =  newCommandObj.build();
        }

        return obj;
    }

    public void addCommand(String command, String description) {
        if ( description == null ) {
            throw new IllegalArgumentException("description is required");
        }
        commands.put(command, description);
    }

    public void removeCommand(String command) {
        commands.remove(command);
    }

    /**
     * Room inventory objects are optional. Build/cache/return a JsonArray listing
     * items in the room for use in location messages.
     * @return JsonArray containing room inventory. Never null
     */
    public JsonArray getInventory() {
        JsonArray arr = null;

        Set<Item> i = items.getItemsInRoom();
        
        Log.log(Level.INFO, this, "getItemsInRoom :: "+i.stream().map(n -> n.getName()).collect(Collectors.toSet()));
        
        if ( i.isEmpty()) {
            return EMPTY_INVENTORY;
        } else if ( arr == null) {
            JsonArrayBuilder newItemArr = Json.createArrayBuilder();
            i.forEach(s -> { newItemArr.add(s.getName()); });
            arr = newItemArr.build();
        }

        return arr;
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("name=").append(name);
        s.append(", fullName=").append(fullName);
        s.append(", description=").append(description);
        s.append(", commands=").append(commands);
        s.append(", items=").append(items);

        return s.toString();
    }
}
