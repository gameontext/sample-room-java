package org.gameontext.sample.it.api;

import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

import org.gameontext.sample.it.EndpointClient;
import org.junit.Test;

public class TestApplicationEndpointOnBluemix extends EndpointClient {

    @Test
    public void indexHtml() {
        String runningInBluemix = System.getProperty("running.bluemix");
        assumeNotNull(runningInBluemix);
        assumeTrue(Boolean.valueOf(runningInBluemix));
        String context = System.getProperty("cf.context.root");
        testEndpoint(context, "index.html", "Your room is running!");
    }

}
