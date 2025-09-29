package com.company.sharefile.dto.v1.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponseDTO {
    private int status;       // Codice di stato HTTP (es. 400, 409)
    private String error;     // Nome generico dell'errore (es. "BAD_REQUEST", "CONFLICT")
    private String message;   // Messaggio dettagliato per il client
    private String internalDocumentationErrorCode;      // Codice di errore interno specifico documentazione (es. "E4001")

    public ErrorResponseDTO(int status, String error, String message, String internalDocumentationErrorCode) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.internalDocumentationErrorCode = internalDocumentationErrorCode;
    }
}
