package com.mycompany.smart.campus.api.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * Global "Catch-all" Exception Mapper to prevent internal leaks.
 * Intercepts any unexpected runtime errors.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        // If it's already a JAX-RS WebApplicationException, we should let it through
        // unless we want to override it specifically.
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }

        // Log the error (in a real app)
        exception.printStackTrace();

        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred. Please contact the administrator.");
        error.put("status", "500");

        // Cybersecurity Note: Avoid returning the stack trace to the client.
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
