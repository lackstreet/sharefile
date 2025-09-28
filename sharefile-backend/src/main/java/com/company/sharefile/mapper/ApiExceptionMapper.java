package com.company.sharefile.mapper;

import com.company.sharefile.dto.v1.ErrorResponseDTO;
import com.company.sharefile.exception.ApiException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.jboss.logging.Logger;

public class ApiExceptionMapper implements ExceptionMapper<ApiException> {

    private static final Logger LOG = Logger.getLogger(ApiExceptionMapper.class);

    @Override
    public Response toResponse(ApiException exception) {

        Response.Status status = exception.getStatus();
        String internalDocumentationErrorCode = exception.getClass().getSimpleName();

        LOG.warnf("Errore  (Codice %s): %s",
                exception.getInternalDocumentationErrorCode(),
                exception.getMessage());

        ErrorResponseDTO errorDTO = new ErrorResponseDTO(
                status.getStatusCode(),
                status.getReasonPhrase().toUpperCase().replace(' ', '_'),
                exception.getMessage(),
                internalDocumentationErrorCode

        );

        return Response
                .status(status)
                .entity(errorDTO)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
