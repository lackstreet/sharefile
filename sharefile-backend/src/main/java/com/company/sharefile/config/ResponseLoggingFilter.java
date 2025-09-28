package com.company.sharefile.config;

import com.company.sharefile.dto.v1.ErrorResponseDTO;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider; // ESSENZIALE
import org.jboss.logging.Logger;

@Provider
public class ResponseLoggingFilter implements ContainerResponseFilter {
    private static final Logger log = Logger.getLogger(ResponseLoggingFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {

        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getAbsolutePath().getPath();
        int status = responseContext.getStatus();

        log.errorf("API_RESPONSE: Metodo=%s, Path=%s, Status=%d", method, path, status);

        if (status >= 400) {
            Object entity = responseContext.getEntity();

            if (entity instanceof ErrorResponseDTO) {
                ErrorResponseDTO errorDTO = (ErrorResponseDTO) entity;
                // Uso di WARN per evidenziare errori client/business (4xx) e
                // di ERROR per problemi interni (5xx)
                log.warnf("Documented error: Response status = %d, " +
                                "Error = %s, " +
                                "Message ='%s' " +
                                "Documented error code = %s",
                        status,
                        errorDTO.getError(),
                        errorDTO.getMessage(),
                        errorDTO.getInternalDocumentationErrorCode());
            }
        }
    }
}