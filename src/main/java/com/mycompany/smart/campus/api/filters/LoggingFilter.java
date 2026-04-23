package com.mycompany.smart.campus.api.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Filter to log incoming requests and outgoing responses.
 * Demonstrated as part of professional practice.
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOG.info(String.format("Incoming Request: %s %s", 
                requestContext.getMethod(), 
                requestContext.getUriInfo().getPath()));
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        LOG.info(String.format("Outgoing Response: %s for %s", 
                responseContext.getStatus(), 
                requestContext.getUriInfo().getPath()));
    }
}
