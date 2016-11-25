package org.gameontext.sample.it.api;

import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeNotNull;

import org.gameontext.sample.it.EndpointClient;
import org.junit.Test;

public class TestApplicationEndpoint extends EndpointClient {

    @Test
    public void indexHtml() {
        String runningInBluemix = System.getProperty("running.bluemix");
        assumeNotNull(runningInBluemix);
        assumeFalse(Boolean.valueOf(runningInBluemix));
        String port = System.getProperty("liberty.test.port");
        testEndpoint("localhost:" + port, "/index.html", "Your room is running!");
    }

}
