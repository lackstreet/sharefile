package com.company.sharefile.config;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider; // NUOVO IMPORT ESSENZIALE
import org.jboss.logging.Logger;

@Provider
public class RequestLoggingFilter implements ContainerRequestFilter {

    private static final Logger log = Logger.getLogger(RequestLoggingFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) {

        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getAbsolutePath().getPath();

        String userId = requestContext.getSecurityContext().getUserPrincipal() != null
                ? requestContext.getSecurityContext().getUserPrincipal().getName()
                : "ANONIMO";

        log.infof("API_CALL: Metodo=%s, Path=%s, Utente=%s", method, path, userId);

        log.debugf("Headers: %s", requestContext.getHeaders().entrySet());
    }
}