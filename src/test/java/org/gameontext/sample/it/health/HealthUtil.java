package org.gameontext.sample.it.health;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;

public class HealthUtil {

	private static String port = System.getProperty("liberty.test.port");
    private static String endpoint = "health";
    private static String url = "https://localhost:" + port + "/" + endpoint;
    
    private static HashMap<String, String> servicesAreUp;
    private static HashMap<String, String> servicesAreDown;

    static {
    	servicesAreUp = new HashMap<String, String>();
    	servicesAreDown = new HashMap<String, String>();

        servicesAreUp.put("RoomEndpoint", "UP");

        servicesAreDown.put("RoomEndpoint", "DOWN");
    }
	
	public int makeRequest() {
	      Client client = ClientBuilder.newClient();
	      Invocation.Builder invoBuild = client.target(url).request();
	      Response response = invoBuild.get();
	      System.out.println(response.getStatus());
	      int responseCode = response.getStatus();
	      response.close();
	      return responseCode;
	    }
    
    
    public JsonArray checkEndPointConnection(int expResponseCode) {
        Client client = ClientBuilder.newClient().register(JsrJsonpProvider.class);
        Response response = client.target(url).request().get();
        assertEquals("No matching response code " + url, expResponseCode, response.getStatus());
        JsonArray servicesJson = response.readEntity(JsonObject.class)
                                           .getJsonArray("checks");
        response.close();
        client.close();
        return servicesJson;
      }
    
    public String existingState(String service, JsonArray servicesJson) {
    	    String actual_state = "";
    	    for (Object obj : servicesJson) {
    	      if (obj instanceof JsonObject) {
    	        if (service.equals(((JsonObject) obj).getString("name"))) {
    	        	actual_state = ((JsonObject) obj).getString("state");
    	        }
    	      }
    	    }
    	    return actual_state;
    	  }
    
    public void checkTheStates(HashMap<String, String> testData, JsonArray servJson) {
        testData.forEach((service, expectedState) -> {
        	System.out.println(service);
        	System.out.println(expectedState);
            assertEquals("The state of " + service + " service is not matching.",
                         expectedState,
                         existingState(service, servJson));
        });
    }
}
