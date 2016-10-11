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
package map.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Exit {
    @JsonProperty("_id")
    String id;
    String name;
    String fullName;
    String door = null;
    ConnectionDetails connectionDetails = null;

    public Exit() {}

    public Exit(Site targetSite, String direction) {
        this.id = targetSite.getId();

        if ( targetSite.getInfo() != null ) {
            this.name = targetSite.getInfo().getName();
            this.fullName = targetSite.getInfo().getFullName();
            this.connectionDetails = targetSite.getInfo().getConnectionDetails();

            if ( targetSite.getInfo().getDoors() != null ) {
                switch(direction.toLowerCase()) {
                    case "n" :
                        this.door = targetSite.getInfo().getDoors().getN();
                        break;
                    case "s" :
                        this.door = targetSite.getInfo().getDoors().getS();
                        break;
                    case "e" :
                        this.door = targetSite.getInfo().getDoors().getE();
                        break;
                    case "w" :
                        this.door = targetSite.getInfo().getDoors().getW();
                        break;
                    case "u" :
                        this.door = targetSite.getInfo().getDoors().getU();
                        break;
                    case "d" :
                        this.door = targetSite.getInfo().getDoors().getD();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown direction for the exit: " + direction);
                }
            }

            // Really generic. They gave us nothing interesting.
            if ( this.door == null )
                this.door = "A door";

            // This won't be the prettiest. ew.
            if ( this.fullName == null )
                this.fullName = this.name;

        } else {
            // Empty/placeholder room. Still navigable if very unclear.
            this.name = "Nether space";
            this.fullName = "Nether space";
            this.door = "Tenuous doorway filled with gray fog";
        }
    }

    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    @JsonProperty("_id")
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDoor() {
        return door;
    }

    public void setDoor(String door) {
        this.door = door;
    }

    public ConnectionDetails getConnectionDetails() {
        return connectionDetails;
    }

    public void setConnectionDetails(ConnectionDetails connectionDetails) {
        this.connectionDetails = connectionDetails;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();
        sb.append("class Exit {");
        sb.append(" id: ").append(id);
        sb.append(", name: ").append(name);
        sb.append(", fullName: ").append(fullName);
        sb.append(", door: ").append(door);
        sb.append("}");
        return sb.toString();
    }
}
