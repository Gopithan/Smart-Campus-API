package com.mycompany.smart.campus.api.config;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * JAX-RS Application configuration.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApp extends ResourceConfig {
    public SmartCampusApp() {
        // Scan for resources and providers in this package
        packages("com.mycompany.smart.campus.api");
        
        // Register Jackson for JSON support
        register(JacksonFeature.class);
    }
}
