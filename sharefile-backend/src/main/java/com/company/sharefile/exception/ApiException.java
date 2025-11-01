package com.company.sharefile.exception;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import org.jboss.logging.Logger;

@Getter
public class ApiException extends RuntimeException {
    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final Response.Status status;
    private final String internalDocumentationErrorCode;

    public ApiException(String message, Response.Status status, String internalDocumentationErrorCode) {
        super(message);
        this.status = status;
        this.internalDocumentationErrorCode = internalDocumentationErrorCode;
        log.errorf("API Exception occurred: %s (Status: %s, Code: %s)", message, status, internalDocumentationErrorCode);
    }


}
