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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * This is how our room is described.
 *  - Update attributes dynamically in {@link RoomImplementation} as the room is used
 *
 *  @see RoomImplementation
 */
public class RoomDescription {

    private final JsonObject EMPTY_COMMANDS = Json.createObjectBuilder().build();
    private final JsonArray EMPTY_INVENTORY = Json.createArrayBuilder().build();

    private String name = "defaultRoomNickName";
    private String fullName = "A room with the default fullName still set in the source";
    private String description = "A room that still has the default description set in the source";

    private Map<String, String> commands = new ConcurrentHashMap<>();
    private JsonObject commandObj = null;

    private Set<String> items = new CopyOnWriteArraySet<>();
    private JsonArray itemObj = null;

    public RoomDescription() {}

    /**
     * @return The room's short name
     */
    public String getName() {
        return name;
    }

    /**
     * The name for a room should match the
     * name it was registered with.
     */
    public void setName(String name){
        if(name!=null){
          this.name = name;
        }
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
        if(fullName!=null){
          this.fullName = fullName;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if(description!=null){
          this.description = description;
        }
    }

    /**
     * Custom commands are optional. Build/cache/return a JsonObject listing
     * commands and a description of what they do for use in location messages
     * @return JsonObject containing custom room commands. Never null.
     */
    public JsonObject getCommands() {
        JsonObject obj = commandObj;

        if ( commands.isEmpty()) {
            return EMPTY_COMMANDS;
        } else if ( obj == null) {
            JsonObjectBuilder newCommandObj = Json.createObjectBuilder();
            commands.entrySet().forEach(e -> { newCommandObj.add(e.getKey(), e.getValue()); });
            obj = commandObj = newCommandObj.build();
        }

        return obj;
    }

    public void addCommand(String command, String description) {
        if ( description == null ) {
            throw new IllegalArgumentException("description is required");
        }
        commands.put(command, description);
        commandObj = null;
    }

    public void removeCommand(String command) {
        commands.remove(command);
        commandObj = null;
    }

    /**
     * Room inventory objects are optional. Build/cache/return a JsonArray listing
     * items in the room for use in location messages.
     * @return JsonArray containing room inventory. Never null
     */
    public JsonArray getInventory() {
        JsonArray arr = itemObj;

        if ( items.isEmpty()) {
            return EMPTY_INVENTORY;
        } else if ( arr == null) {
            JsonArrayBuilder newItemArr = Json.createArrayBuilder();
            items.forEach(s -> { newItemArr.add(s); });
            arr = itemObj = newItemArr.build();
        }

        return arr;
    }

    public void addItem(String itemName) {
        items.add(itemName);
        itemObj = null;
    }

    public void removeItem(String itemName) {
        items.remove(itemName);
        itemObj = null;
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
