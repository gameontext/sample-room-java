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

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.MetricUnits;

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
     @Counted(name="getName.counted",
             absolute = true,
             displayName="getName Count",
             description="Number of times getName is called",
             monotonic=true)
    public String getName() {
        return name;
    }

    /**
     * The name for a room should match the
     * name it was registered with.
     */
     @Counted(name="setName.counted",
             absolute = true,
             displayName="setName Count",
             description="Number of times setName is called.",
             monotonic=true)
    public void setName(String name){
        if(name!=null){
          this.name = name;
        }
    }

    /**
     * @return The room's long name
     */
     @Counted(name="getFullName.counted",
             absolute = true,
             displayName="getFullName Count",
             description="Number of times getFullName is called.",
             monotonic=true)
    public String getFullName() {
        return fullName;
    }

    /**
     * The display name for a room can change at any time.
     * @param fullName A new display name for the room
     */
     @Counted(name="setFullName.counted",
             absolute = true,
             displayName="setFullName Count",
             description="Number of times setFullName is called.",
             monotonic=true)
    public void setFullName(String fullName) {
        if(fullName!=null){
          this.fullName = fullName;
        }
    }

    @Counted(name="getDescription.counted",
            absolute = true,
            displayName="getDescription Count",
            description="Number of times getDescription is called.",
            monotonic=true)
    public String getDescription() {
        return description;
    }

    @Counted(name="setDescription.counted",
            absolute = true,
            displayName="setDescription Count",
            description="Number of times setDescription is called.",
            monotonic=true)
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
     @Timed(name = "getCommands.timer",
             absolute = true,
             displayName="getCommands Timer",
             description = "Time taken by getCommands")
     @Counted(name="getCommands.counted",
             absolute = true,
             displayName="getCommands Count",
             description="Number of times getCommands is called.",
             monotonic=true)
     @Metered(name="getCommands",
             displayName="getCommands Frequency",
             description="Average time to call getCommands")
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

    @Counted(name="addCommand.counted",
            absolute = true,
            displayName="addCommand Count",
            description="Number of times addCommand is called.",
            monotonic=true)
    public void addCommand(String command, String description) {
        if ( description == null ) {
            throw new IllegalArgumentException("description is required");
        }
        commands.put(command, description);
        commandObj = null;
    }

    @Counted(name="removeCommand.counted",
            absolute = true,
            displayName="removeCommand Count",
            description="Number of times removeCommand is called.",
            monotonic=true)
    public void removeCommand(String command) {
        commands.remove(command);
        commandObj = null;
    }

    /**
     * Room inventory objects are optional. Build/cache/return a JsonArray listing
     * items in the room for use in location messages.
     * @return JsonArray containing room inventory. Never null
     */
     @Timed(name = "getInventory.timer",
             absolute = true,
             displayName="getInventory Timer",
             description = "Time taken by getInventory")
     @Counted(name="getInventory.counted",
             absolute = true,
             displayName="getInventory Count",
             description="Number of times getInventory is called.",
             monotonic=true)
     @Metered(name="getInventory",
             displayName="getInventory Frequency",
             description="Average time to call getInventory")
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
    
    @Counted(name="addItem.counted",
            absolute = true,
            displayName="addItem Count",
            description="Number of times addItem is called.",
            monotonic=true)
    public void addItem(String itemName) {
        items.add(itemName);
        itemObj = null;
    }
    
    @Counted(name="removeItem.counted",
            absolute = true,
            displayName="removeItem Count",
            description="Number of times removeItem is called.",
            monotonic=true)
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
