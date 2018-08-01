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
package org.gameontext.sample.it.health;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import javax.json.JsonArray;

import org.junit.Test;

public class HealthEndpointIT {

    HealthUtil healthUitl = mock(HealthUtil.class);
    
    private JsonArray servicesStates;
    private static HashMap<String, String> servicesAreUp;
    private static HashMap<String, String> servicesAreDown;

    static {
    	servicesAreUp = new HashMap<String, String>();
    	servicesAreDown = new HashMap<String, String>();

        servicesAreUp.put("RoomEndpoint", "UP");

        servicesAreDown.put("RoomEndpoint", "DOWN");
    }

    @Test
    public void testIfServicesAreUp() {
    	when(healthUitl.makeRequest()).thenReturn(200);
    	int responseCode = healthUitl.makeRequest();
        servicesStates = healthUitl.checkEndPointConnection(responseCode);
        healthUitl.checkTheStates(servicesAreUp, servicesStates);
    }
    
    @Test
    public void testIfInventoryServiceIsDown() {
    	when(healthUitl.makeRequest()).thenReturn(503);
    	int responseCode = healthUitl.makeRequest();
        servicesStates = healthUitl.checkEndPointConnection(responseCode);
        healthUitl.checkTheStates(servicesAreDown, servicesStates);
    }
}
