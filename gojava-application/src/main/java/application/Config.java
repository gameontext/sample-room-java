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
package application;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 * Manages obtaining the configuration from the environment and 
 * generating any necessary data for a successful room registration.
 * 
 */
public class Config {
	//You should change this name to avoid confusion with other sample rooms registered with Game On.
    private final String name = "AnotherSimpleRoom";
    private final String fullName = "A Very Simple Room.";
    private final String description = "You are in the worlds most simple room, there is nothing to do here.";
	
    //default registration is with the live site, although this can be changed via environment variables
    private String registrationUrl = "https://game-on.org/map/v1/sites";
    private String endPointUrl;
    private String userId;
    private String key;
    private final boolean valid;

    public Config(final ServletContextEvent e) {
        // credentials, read from the cf environment, and
        // originally obtained from the gameon instance to connect to.
        userId = System.getenv("GAMEON_ID");
        key = System.getenv("GAMEON_SECRET");
        String url = System.getenv("GAMEON_MAP_URL");
        if(url != null) {
        	registrationUrl = url;	//map registration URL has been overwritten by an env var
        }
        if(userId == null || key == null){
            System.out.println("This room is intended to obtain it's configuration from the environment");
            System.out.println("GameOn! userid or secret is missing from the environment.");
            System.out.println("You could have supplied these via `-DGAMEON_ID=myid -DGAMEON_SECRET=mysecret` when running mvn install");
            System.out.println("Or you could configure them directly in the environment variables panel from your bluemix console for the app");
            valid = false;
            return;
        }

        //if we're running in a cf, we should use the details from those environment vars.
        String vcap_application = System.getenv("VCAP_APPLICATION");
        if(vcap_application!=null){
            ServletContext sc = e.getServletContext();
            String contextPath = sc.getContextPath();

            JsonObject vcapApplication = Json.createReader(new StringReader(vcap_application)).readObject();
            JsonArray uris = vcapApplication.getJsonArray("application_uris");
            JsonString firstUriAsString = uris.getJsonString(0);
            endPointUrl = "ws://"+firstUriAsString.getString()+contextPath+"/room";
            System.out.println("Using CF details of "+endPointUrl);
        }else{
            System.out.println("This room is intended to obtain it's configuration from the CF environment");
            System.out.println("Assuming that this room is running on localhost port 9080 (this should match the config in server.xml)");
            endPointUrl = "ws://localhost:9080/room";
        }
        valid = true;
    }
    
    /**
     * Builds the JSON object which will be used for the room registration. You can change the values to customise
     * your room, at the very least, you should change the name of the room so that it stands out
     * from the other sample rooms.
     * 
     * @return JSON for the room registration
     */
    public String getRoomJSON() {
    	// build the registration payload (post data)
        JsonObjectBuilder registrationPayload = Json.createObjectBuilder();
        // add the basic room info.
        registrationPayload.add("name", name);
        registrationPayload.add("fullName", fullName);
        registrationPayload.add("description", description);
        // add the doorway descriptions we'd like the game to use if it
        // wires us to other rooms. Note: you're describing the outside 
        // of your room: What does the North wall+door of your room look
        // like from the outside? Note that a traveller will be looking South
        // when they view it!
        JsonObjectBuilder doors = Json.createObjectBuilder();
        doors.add("n", "A Large doorway to the south");
        doors.add("s", "A winding path leading off to the north");
        doors.add("e", "An overgrown road, covered in brambles");
        doors.add("w", "A shiny metal door, with a bright red handle");
        doors.add("u", "A tunnel, leading down into the earth");
        doors.add("d", "A spiral set of stairs, leading upward into the ceiling");
        registrationPayload.add("doors", doors.build());

        // add the connection info for the room to connect back to us..
        JsonObjectBuilder connInfo = Json.createObjectBuilder();
        connInfo.add("type", "websocket"); // the only current supported
                                           // type.
        connInfo.add("target", endPointUrl);
        registrationPayload.add("connectionDetails", connInfo.build());

        return registrationPayload.build().toString();
    }

	public String getRegistrationUrl() {
		return registrationUrl;
	}

	public String getEndPointUrl() {
		return endPointUrl;
	}

	public void setEndPointUrl(String endPointUrl) {
		this.endPointUrl = endPointUrl;
	}

	/**
	 * Determine if this configuration object is valid, i.e. has all it's required component
	 * parts such as the Game On ID and shared secret. If the object isn't valid, then it's
	 * likely an issue with the running environment.
	 * @return
	 */
	public boolean isValid() {
		return valid;
	}

	public String getUserId() {
		return userId;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public String getFullname() {
		return fullName;
	}

	public String getDescription() {
		return description;
	}
	
	
}
