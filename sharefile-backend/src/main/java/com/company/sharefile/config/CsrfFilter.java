package com.company.sharefile.config;

import com.company.sharefile.service.CsrfService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.Map;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class CsrfFilter implements ContainerRequestFilter {

    @Inject
    CsrfService csrfService;

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String REFRESH_PATH = "/api/v1/auth/refresh";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if ("GET".equals(requestContext.getMethod())) {
            return;
        }
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        String path = requestContext.getUriInfo().getPath();

        if (LOGIN_PATH.equals(path) || REFRESH_PATH.equals(path)) {
            return;
        }

        String csrfHeader = requestContext.getHeaderString("X-CSRF-Token");
        String csrfCookie = requestContext.getCookies().getOrDefault("csrf_token", null) != null
                ? requestContext.getCookies().get("csrf_token").getValue()
                : null;

        if (!csrfService.verify(csrfHeader, csrfCookie)) {
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Invalid CSRF token"))
                    .build());
        }
    }

}