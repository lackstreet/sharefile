package com.company.sharefile.exception;

import jakarta.ws.rs.core.Response;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final Response.Status status;
    private final String internalDocumentationErrorCode;

    public ApiException(String message, Response.Status status, String internalDocumentationErrorCode) {
        super(message);
        this.status = status;
        this.internalDocumentationErrorCode = internalDocumentationErrorCode;
    }


}
