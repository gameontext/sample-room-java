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
package org.gameontext.sample.map.client;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.cache.annotation.CacheResult;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gameontext.sample.Log;

import net.jodah.failsafe.CircuitBreaker;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

/**
 * A wrapped/encapsulation of outbound REST requests to the map service.
 * <p>
 * The URL for the map service and the API key are injected via CDI: {@code
 * <jndiEntry />} elements defined in server.xml maps the environment variables
 * to JNDI values.
 * </p>
 * <p>
 * CDI will create this (the {@code MapClient} as an application scoped bean.
 * This bean will be created when the application starts, and can be injected
 * into other CDI-managed beans for as long as the application is valid.
 * </p>
 *
 * @see ApplicationScoped
 */
@ApplicationScoped
public class MapClient {

    public static final String DEFAULT_MAP_URL = "https://gameontext.org/map/v1/sites";

    /**
     * The URL for the target map service.
     * This is set via the environment variable MAP_URL. This value is read
     * in server.xml.
     */
    //@Resource(lookup = "mapUrl")
    private String mapLocation = DEFAULT_MAP_URL;
    
    @Resource(lookup = "serverDir")
    private String serverDir;

    /**
     * The root target used to define the root path and common query parameters
     * for all outbound requests to the concierge service.
     *
     * @see WebTarget
     */
    private WebTarget queryRoot;
    
    /**
     * The retry policy for failsafe.
     */
    private RetryPolicy retryPolicy;
    /**
     * The circuit breaker for failsafe.
     */
    private CircuitBreaker breaker;

    /**
     * The {@code @PostConstruct} annotation indicates that this method should
     * be called immediately after the {@code MapClient} is instantiated
     * with the default no-argument constructor.
     *
     * @see PostConstruct
     * @see ApplicationScoped
     */
    @SuppressWarnings("unchecked")
    @PostConstruct
    public void initClient() {
        if (mapLocation == null) {
            MapClientLog.log(Level.FINER, this, "No MAP_URL environment variable provided. Will use default.");
            mapLocation = DEFAULT_MAP_URL;
        }

        MapClientLog.log(Level.INFO, this, "Map URL set to {0}", mapLocation);

        
        Client queryClient = ClientBuilder.newBuilder()
                .property("com.ibm.ws.jaxrs.client.ssl.config", "DefaultSSLSettings")
                .property("com.ibm.ws.jaxrs.client.disableCNCheck", true)
                .build();

        queryClient.register(MapResponseReader.class);

        // create the jax-rs 2.0 client
        this.queryRoot = queryClient.target(mapLocation);
        
        // create the retry policy & circuit breaker for failsafe.
        retryPolicy = new RetryPolicy()
                .retryOn(ServiceUnavailableException.class)
                .withDelay(1, TimeUnit.SECONDS)
                .withMaxRetries(3);
        breaker = new CircuitBreaker()
                .withFailureThreshold(3, 10)
                .withSuccessThreshold(5)
                .withDelay(1,  TimeUnit.MINUTES)
                ;

        
        MapClientLog.log(Level.FINER, this, "Map client initialized");
    }
    
    @CacheResult
    public String getValue(String key){
        String r = "KEY:"+key+"VALUE:"+System.currentTimeMillis();
        Log.log(Level.INFO, this, "getValue return "+r);
        return r;
    }
    

    public MapData getMapData(String siteId) {
        WebTarget target = this.queryRoot.path(siteId);
        MapClientLog.log(Level.FINER, this, "making request to {0} for room", target.getUri().toString());
        
        return Failsafe.with(retryPolicy).with(breaker).get( () -> {
            try {
                Response r = null;
                r = target.request(MediaType.APPLICATION_JSON).get();
                if (r.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                    MapData data = r.readEntity(MapData.class);
                    return data;
                }
                return null;
            } catch (ResponseProcessingException rpe) {
                Response response = rpe.getResponse();
                MapClientLog.log(Level.FINER, this, "Exception fetching room list uri: {0} resp code: {1} ",
                        target.getUri().toString(),
                        response.getStatusInfo().getStatusCode() + " " + response.getStatusInfo().getReasonPhrase());
                if(503 == response.getStatusInfo().getStatusCode()){
                    throw new ServiceUnavailableException();
                }
                MapClientLog.log(Level.FINEST, this, "Exception fetching room list", rpe);
            } catch (ProcessingException e) {
                MapClientLog.log(Level.FINEST, this, "Exception fetching room list (" + target.getUri().toString() + ")", e);
            } catch (WebApplicationException ex) {
                MapClientLog.log(Level.FINEST, this, "Exception fetching room list (" + target.getUri().toString() + ")", ex);
            }
            // Sadly, badness happened while trying to get the endpoints
            return null;
            
        });
        
    }
}