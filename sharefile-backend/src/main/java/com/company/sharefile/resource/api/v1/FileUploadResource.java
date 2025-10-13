package com.company.sharefile.resource;

import com.company.sharefile.dto.v1.records.response.FileUploadResponseDTO;
import com.company.sharefile.service.FileUploadService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.logging.Logger;

import java.io.File;

@Path("/api/v1/files")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class FileUploadResource {

    @Inject
    Logger log;

    @Inject
    FileUploadService fileUploadService;

    @POST
    @Path("/upload/{fileId}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Upload file content")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "File uploaded successfully"),
            @APIResponse(responseCode = "404", description = "File ID not found"),
            @APIResponse(responseCode = "409", description = "File already uploaded"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response uploadFile(
            @PathParam("fileId") String fileId,
            File uploadedFile) {

        log.infof("Received file upload for fileId: %s, size: %d bytes",
                fileId, uploadedFile.length());

        try {
            FileUploadResponseDTO response = fileUploadService.uploadFileFromTemp(fileId, uploadedFile);

            return Response.ok(response).build();

        } catch (Exception e) {
            log.errorf(e, "Error uploading file %s", fileId);
            return Response.serverError()
                    .entity(new ErrorResponse(e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{fileId}/status")
    public Response getFileStatus(@PathParam("fileId") String fileId) {
        // TODO: implementare
        return Response.ok().build();
    }

    public record ErrorResponse(String error) {}
}