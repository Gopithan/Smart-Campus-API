package com.mycompany.smart.campus.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Discovery endpoint for the Smart Campus API.
 * Provides metadata and entry points to resources.
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> discover() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("api_version", "v1.0.0");
        metadata.put("organization", "University of Westminster");
        metadata.put("contact", "admin@smartcampus.westminster.ac.uk");
        
        Map<String, String> resources = new HashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        
        metadata.put("resources", resources);
        metadata.put("message", "Welcome to the Smart Campus API. Explore the resources provided above.");
        
        return metadata;
    }
}
