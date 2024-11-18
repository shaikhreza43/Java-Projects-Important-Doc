package com.java.util;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

// This is a Centralized Logging Filter class which will work in Java Quarkus projects

@Provider
@Slf4j
public class LoggingFilter
        implements ContainerRequestFilter, ContainerResponseFilter, ClientRequestFilter, ClientResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        log.info("ContainerRequestContext logging Starts...");

        // Read and log request body safely
        String requestBody = getRequestBodyAsString(requestContext);
        log.info("Request Body: " + requestBody);

        log.info("Request Uri: {}", requestContext.getUriInfo().getPath());
        log.info("Request Headers: {}", requestContext.getHeaders());
        log.info("Request Method: {}", requestContext.getMethod());
        log.info("Request Path Params: {}", requestContext.getUriInfo().getPathParameters());
        log.info("Request Absolute Path: {}", requestContext.getUriInfo().getAbsolutePath());
        log.info("Request Query params: {}", requestContext.getUriInfo().getQueryParameters());

        log.info("ContainerRequestContext logging Ends...");
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        log.info("ContainerResponseContext logging Starts...");

        log.info("Response Status: {}", responseContext.getStatus());
        log.info("Response Headers: {}", responseContext.getHeaders());
        log.info("Response Media Type: {}", responseContext.getMediaType());

        // Log response body
        Object entity = responseContext.getEntity();
        if (entity != null) {
            log.info("Response Body: {}", entity.toString());
        } else {
            log.info("Response Body: null");
        }

        log.info("ContainerResponseContext logging Ends...");
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            responseContext.getEntityStream().transferTo(baos);
            String responseBody = baos.toString(StandardCharsets.UTF_8);

            if (responseContext.hasEntity()) {
                printClientResponseLogs(requestContext, responseContext, responseBody);
            }

            // Reset the stream for further processing
            responseContext.setEntityStream(new ByteArrayInputStream(baos.toByteArray()));
        } catch (IOException e) {
            log.error("Error occured while tranforming the response stream..." + e.getMessage());
        } catch (Exception e) {
            log.error("Error occured..." + e.getMessage());
        }
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        log.info("ClientRequestContext Logging Starts...");
        log.info("Client Request Entity: {}", requestContext.getEntity());
        log.info("Client Request Method: {}", requestContext.getMethod());
        log.info("Client Request Uri: {}", requestContext.getUri());
        log.info("ClientRequestContext Logging Ends...");
    }

    private void printClientResponseLogs(ClientRequestContext requestContext,
            ClientResponseContext responseContext, String responseBody) {

        log.info("ClientResponseContext Logging Starts...");
        log.info("Client Response Body: {}", responseBody);
        log.info("Client Response Status: {}", responseContext.getStatus());
        log.info("ClientResponseContext Logging Ends...");

    }

    /**
     * Helper method to read and log the request body safely.
     *
     * @param requestContext The ContainerRequestContext object.
     * @return The request body as a string.
     * @throws IOException If an I/O error occurs while reading the request body.
     */
    private String getRequestBodyAsString(ContainerRequestContext requestContext) throws IOException {
        String requestBody = null;

        try (InputStream inputStream = requestContext.getEntityStream()) {
            if (inputStream != null) {

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy(inputStream, baos);
                requestBody = baos.toString(StandardCharsets.UTF_8);

                InputStream newInputStream = new ByteArrayInputStream(baos.toByteArray());
                requestContext.setEntityStream(newInputStream);

            } else {
                log.warn("Input stream is null");
            }

        } catch (IOException e) {
            log.error("Error reading request body", e);
            throw e;
        }

        return requestBody;
    }

}
