package com.griddynamics.jdemo;

import com.griddynamics.util.JaggerPropertiesProvider;
import com.griddynamics.jagger.invoker.v2.JHttpEndpoint;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An example of endpoint provider.
 * Provides endpoints for {@link JLoadScenarioProvider}.
 */
// begin: following section is used for docu generation - Endpoint provider
public class SimpleEndpointsProvider implements Iterable  {

    private List<JHttpEndpoint> endpoints = new ArrayList<>();

    // Simple example of endpoint provider
    // Constructor will be triggered during spring bean creation at Jagger startup
    // Later distributor will invoke iterator method to get endpoints
    public SimpleEndpointsProvider(JaggerPropertiesProvider provider) {
        JHttpEndpoint httpEndpoint = new JHttpEndpoint(URI.create(provider.getTestPropertyValue("p1.aut.url")));
        endpoints.add(httpEndpoint);
    }
    
    @Override
    public Iterator<JHttpEndpoint> iterator() {
        return endpoints.iterator();
    }

}
// end: following section is used for docu generation - Endpoint provider
