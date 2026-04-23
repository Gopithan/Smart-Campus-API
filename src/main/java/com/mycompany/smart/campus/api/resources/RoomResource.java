package com.mycompany.smart.campus.api.resources;

import com.mycompany.smart.campus.api.exceptions.RoomNotEmptyException;
import com.mycompany.smart.campus.api.models.Room;
import com.mycompany.smart.campus.api.db.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Resource for managing Rooms at /api/v1/rooms.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    /**
     * GET /api/v1/rooms: List all rooms.
     */
    @GET
    public List<Room> getAllRooms() {
        return new ArrayList<>(DataStore.getRooms().values());
    }

    /**
     * POST /api/v1/rooms: Create a new room.
     */
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Room ID is required")
                    .build();
        }
        DataStore.addRoom(room);
        return Response.status(Response.Status.CREATED)
                .entity(room)
                .build();
    }

    /**
     * GET /api/v1/rooms/{id}: Get detailed metadata for a specific room.
     */
    @GET
    @Path("/{id}")
    public Response getRoom(@PathParam("id") String id) {
        Room room = DataStore.getRoom(id);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Room with ID " + id + " not found")
                    .build();
        }
        return Response.ok(room).build();
    }

    /**
     * DELETE /api/v1/rooms/{id}: Decommission a room.
     * Safety Logic: Cannot delete if active sensors are assigned.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        Room room = DataStore.getRoom(id);
        if (room == null) {
            // DELETE is idempotent, so returning 204 even if not found is standard,
            // but for coursework clarity, we might want to return 404 or just 204.
            // I'll return 204 No Content for idempotency justification later.
            return Response.noContent().build();
        }

        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Cannot delete room '" + id + "' because it currently contains " 
                    + room.getSensorIds().size() + " sensors.");
        }

        DataStore.deleteRoom(id);
        return Response.noContent().build();
    }
}
