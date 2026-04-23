package com.mycompany.smart.campus.api.resources;

import com.mycompany.smart.campus.api.exceptions.SensorUnavailableException;
import com.mycompany.smart.campus.api.models.Sensor;
import com.mycompany.smart.campus.api.models.SensorReading;
import com.mycompany.smart.campus.api.db.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * Sub-resource for managing readings of a specific sensor.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{id}/readings: Fetch history for this sensor.
     */
    @GET
    public List<SensorReading> getReadings() {
        return DataStore.getReadings(sensorId);
    }

    /**
     * POST /api/v1/sensors/{id}/readings: Append a new reading.
     * Side Effect: Updates parent Sensor's currentValue.
     * Logic: Blocks if sensor is in MAINTENANCE status.
     */
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.getSensor(sensorId);
        
        // State constraint: Cannot accept readings if MAINTENANCE
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor '" + sensorId 
                    + "' is currently in MAINTENANCE mode and cannot accept new readings.");
        }

        // Generate ID and timestamp if missing
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        DataStore.addReading(sensorId, reading);
        
        return Response.status(Response.Status.CREATED)
                .entity(reading)
                .build();
    }
}
