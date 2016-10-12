package map.client;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import map.client.model.ConnectionDetails;
import map.client.model.Doors;
import map.client.model.Exit;
import map.client.model.Exits;
import map.client.model.RoomInfo;
import map.client.model.Site;

@ApplicationScoped
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class MapResponseReader implements MessageBodyReader<Site> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(Site.class);
    }

    @Override
    public Site readFrom(Class<Site> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        JsonReader rdr = null;
        try {
            rdr = Json.createReader(entityStream);
            JsonStructure json = rdr.read();
            JsonObject returnedJson = (JsonObject) json;
            Site site = new Site();

            RoomInfo roomInfo = getRoomInfo(returnedJson);
            site.setInfo(roomInfo);
            
            Exits exits = getExits(returnedJson);
            site.setExits(exits);
            
            site.setOwner(sanitiseNull(returnedJson.getJsonString("owner")));
            site.setType(sanitiseNull(returnedJson.getJsonString("type")));
            site.setId(sanitiseNull(returnedJson.getJsonString("_id")));

            return site;
        } finally {
            if (rdr != null) {
                rdr.close();
            }
        }
    }

    private Exits getExits(JsonObject returnedJson) {
        JsonObject exitsObj = returnedJson.getJsonObject("info");
        Exits exits = new Exits();
        if (exitsObj != null) {
            Exit n = getExit(exitsObj, "n");
            exits.setN(n);
            
            Exit s = getExit(exitsObj, "s");
            exits.setS(s);
            
            Exit e = getExit(exitsObj, "e");
            exits.setE(e);
            
            Exit w = getExit(exitsObj, "w");
            exits.setW(w);
            
            Exit u = getExit(exitsObj, "u");
            exits.setU(u);
            
            Exit d = getExit(exitsObj, "d");
            exits.setD(d);
        }
        return exits;
    }

    private Exit getExit(JsonObject exitsObj, String exitDirection) {
        JsonObject exitObj = exitsObj.getJsonObject(exitDirection);
        Exit exit = new Exit();
        if (exitObj != null) {
            exit.setDoor(sanitiseNull(exitObj.getJsonString("door")));
            exit.setFullName(sanitiseNull(exitObj.getJsonString("fullName")));
            exit.setName(sanitiseNull(exitObj.getJsonString("name")));
            exit.setId(sanitiseNull(exitObj.getJsonString("_id")));
            ConnectionDetails connectionDetails = getConnectionDetails(exitObj);
            exit.setConnectionDetails(connectionDetails);
        }
        return null;
    }

    private ConnectionDetails getConnectionDetails(JsonObject exitObj) {
        ConnectionDetails connectionDetails = new ConnectionDetails();
        JsonObject connectionDetailsObj = exitObj.getJsonObject("connectionDetails");
        if (connectionDetailsObj != null) {
            connectionDetails.setType(sanitiseNull(connectionDetailsObj.getJsonString("type")));
            connectionDetails.setTarget(sanitiseNull(connectionDetailsObj.getJsonString("target")));
            connectionDetails.setToken(sanitiseNull(connectionDetailsObj.getJsonString("token")));
        }
        return connectionDetails;
    }

    private RoomInfo getRoomInfo(JsonObject returnedJson) {
        JsonObject info = returnedJson.getJsonObject("info");
        RoomInfo roomInfo = new RoomInfo();
        if (info != null) {
            roomInfo.setName(sanitiseNull(info.getJsonString("name")));
            roomInfo.setFullName(sanitiseNull(info.getJsonString("fullName")));
            roomInfo.setDescription(sanitiseNull(info.getJsonString("description")));
            JsonObject doorsObj = info.getJsonObject("doors");
            Doors doors = getDoors(doorsObj);
            roomInfo.setDoors(doors);
        }
        return roomInfo;
    }

    private Doors getDoors(JsonObject doorsObj) {
        Doors doors = new Doors();
        doors.setD(sanitiseNull(doorsObj.getJsonString("d")));
        doors.setU(sanitiseNull(doorsObj.getJsonString("u")));
        doors.setN(sanitiseNull(doorsObj.getJsonString("n")));
        doors.setS(sanitiseNull(doorsObj.getJsonString("s")));
        doors.setE(sanitiseNull(doorsObj.getJsonString("e")));
        doors.setW(sanitiseNull(doorsObj.getJsonString("w")));
        return doors;
    }

    private String sanitiseNull(JsonString jsonString) {
        return (jsonString == null) ? null : jsonString.getString();
    }
}
