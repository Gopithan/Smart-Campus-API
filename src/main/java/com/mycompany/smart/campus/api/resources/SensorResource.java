package com.mycompany.smart.campus.api.resources;

import com.mycompany.smart.campus.api.exceptions.LinkedResourceNotFoundException;
import com.mycompany.smart.campus.api.models.Sensor;
import com.mycompany.smart.campus.api.db.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resource for managing Sensors at /api/v1/sensors.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    @Context
    private UriInfo uriInfo;

    /**
     * GET /api/v1/sensors: List all sensors with optional type filtering.
     */
    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        List<Sensor> allSensors = new ArrayList<>(DataStore.getSensors().values());
        if (type != null && !type.isEmpty()) {
            return allSensors.stream()
                    .filter(s -> type.equalsIgnoreCase(s.getType()))
                    .collect(Collectors.toList());
        }
        return allSensors;
    }

    /**
     * POST /api/v1/sensors: Register a new sensor.
     * Logic: Must verify roomld exists.
     */
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Sensor ID is required")
                    .build();
        }
        
        // Integrity check: Verify roomId exists
        if (DataStore.getRoom(sensor.getRoomId()) == null) {
            throw new LinkedResourceNotFoundException("Cannot register sensor '" + sensor.getId() 
                    + "' because the specified Room ID '" + sensor.getRoomId() + "' does not exist.");
        }

        DataStore.addSensor(sensor);
        return Response.status(Response.Status.CREATED)
                .entity(sensor)
                .build();
    }

    /**
     * GET /api/v1/sensors/{id}: Get specific sensor detail.
     */
    @GET
    @Path("/{id}")
    public Response getSensor(@PathParam("id") String id) {
        Sensor sensor = DataStore.getSensor(id);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Sensor with ID " + id + " not found")
                    .build();
        }
        return Response.ok(sensor).build();
    }

    /**
     * SUB-RESOURCE LOCATOR: /api/v1/sensors/{id}/readings
     */
    @Path("/{id}/readings")
    public SensorReadingResource getReadingResource(@PathParam("id") String sensorId) {
        // Verify sensor exists before delegating
        if (DataStore.getSensor(sensorId) == null) {
            throw new WebApplicationException("Sensor with ID " + sensorId + " not found", Response.Status.NOT_FOUND);
        }
        return new SensorReadingResource(sensorId);
    }
}
