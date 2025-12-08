package com.company.sharefile.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CORSFilter implements ContainerResponseFilter {

    private static final String ALLOWED_ORIGIN = "http://localhost:4200";

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {

        // Imposta solo se non già presente
        setHeaderIfAbsent(responseContext, "Access-Control-Allow-Origin", ALLOWED_ORIGIN);
        setHeaderIfAbsent(responseContext, "Access-Control-Allow-Credentials", "true");
        setHeaderIfAbsent(responseContext, "Access-Control-Allow-Headers",
                "Origin, Content-Type, Accept, Authorization");
        setHeaderIfAbsent(responseContext, "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS");

        // Preflight: risposta immediata
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            responseContext.setStatus(200);
        }
    }

    private void setHeaderIfAbsent(ContainerResponseContext responseContext,
                                   String header, String value) {
        if (!responseContext.getHeaders().containsKey(header)) {
            responseContext.getHeaders().add(header, value);
        }
    }
}