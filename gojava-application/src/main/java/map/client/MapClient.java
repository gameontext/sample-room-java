/*******************************************************************************
 * Copyright (c) 2015 IBM Corp.
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
package map.client;

import java.util.logging.Level;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import map.client.model.Site;

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

	private static final String mapLocation = "https://game-on.org/map/v1/sites";

    /**
     * The root target used to define the root path and common query parameters
     * for all outbound requests to the concierge service.
     *
     * @see WebTarget
     */
    private WebTarget queryRoot;

    /**
     * The {@code @PostConstruct} annotation indicates that this method should
     * be called immediately after the {@code MapClient} is instantiated
     * with the default no-argument constructor.
     *
     * @see PostConstruct
     * @see ApplicationScoped
     */
    @PostConstruct
    public void initClient() {
        if ( mapLocation == null ) {
            throw new IllegalStateException("Map client can not be initialized, 'mapUrl' is not defined");
        }

        Client queryClient = ClientBuilder.newBuilder()
                                          .property("com.ibm.ws.jaxrs.client.ssl.config", "DefaultSSLSettings")
                                          .property("com.ibm.ws.jaxrs.client.disableCNCheck", true)
                                          .build();

        queryClient.register(MapResponseReader.class);

        // create the jax-rs 2.0 client
        this.queryRoot = queryClient.target(mapLocation);

        Log.log(Level.FINER, this, "Map client initialized with url {0}", mapLocation);
    }

    public Site getSite(String siteId) {
        WebTarget target = this.queryRoot.path(siteId);
        Site ns = getSite(target);
    	return ns;
    }
    /**
     * Invoke the provided {@code WebTarget}, and resolve/parse the result into
     * a {@code Site} that the caller can use to create a new
     * connection to the target room.
     *
     * @param target
     *            {@code WebTarget} that includes the required parameters to
     *            retrieve information about available or specified exits. All
     *            of the REST requests that find or work with exits return the
     *            same result structure
     * @return A populated {@code Site}, or null if the request
     *         failed.
     */
    private Site getSite(WebTarget target) {
        Log.log(Level.FINER, this, "making request to {0} for room", target.getUri().toString());
        Response r = null;
        try {
            r = target.request(MediaType.APPLICATION_JSON).get();
            if (r.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                Site site = r.readEntity(Site.class);
                return site;
            }
            return null;
        } catch (ResponseProcessingException rpe) {
            Response response = rpe.getResponse();
            Log.log(Level.FINER, this, "Exception fetching room list uri: {0} resp code: {1} ",
                    target.getUri().toString(),
                    response.getStatusInfo().getStatusCode() + " " + response.getStatusInfo().getReasonPhrase());
            Log.log(Level.FINEST, this, "Exception fetching room list", rpe);
        } catch (ProcessingException e) {
            Log.log(Level.FINEST, this, "Exception fetching room list (" + target.getUri().toString() + ")", e);
        } catch (WebApplicationException ex) {
            Log.log(Level.FINEST, this, "Exception fetching room list (" + target.getUri().toString() + ")", ex);
        }
        // Sadly, badness happened while trying to get the endpoints
        return null;
    }
}