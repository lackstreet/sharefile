package com.company.sharefile.resource.api.v1;

import com.company.sharefile.entity.FileEntity;
import com.company.sharefile.exception.ApiException;
import com.company.sharefile.service.FileService;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Path("/api/v1/upload")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class UploadResource {

    @Inject
    Logger log;

    @Inject
    FileService fileService;

    @Inject
    SecurityIdentity securityIdentity;

    @POST
    @Path("/file")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(
            @NotBlank(message = "fileName is required")
            @Size(max = 255, message = "fileName max 255 characters")
            @QueryParam("filename")
            String fileName,

            @QueryParam("mimetype")
            @DefaultValue("application/octet-stream")
            String mimeType,

            @NotNull InputStream fileData) {
        log.infof("Received upload request for file: %s", fileName);
        try{
            OidcJwtCallerPrincipal principal = (OidcJwtCallerPrincipal) securityIdentity.getPrincipal();
            String keycloakId = principal.getClaim(Claims.sub.name());

            String sanitizedFileName = fileName.trim();
            if(fileData == null){
                throw new ApiException(
                        String.format("File %s is empty", sanitizedFileName),
                        Response.Status.BAD_REQUEST,
                        "LAM-400-001"
                );
            }

            byte[] fileBytes = fileData.readAllBytes();
            long fileSize = fileBytes.length;
            log.infof("   File size: %d bytes", fileSize);

            if(fileSize > 500 * 1024 * 1024){ // 500 MB limit
                throw new ApiException(
                        String.format("File %s exceeds the maximum allowed size of 500MB", sanitizedFileName),
                        Response.Status.BAD_REQUEST,
                        "LAM-400-003"
                );
            }

            InputStream inputStream = new ByteArrayInputStream(fileBytes);
            FileEntity uploadedFile = fileService.uploadFile(inputStream,sanitizedFileName,fileSize, mimeType, keycloakId);

            log.infof("REST: File uploaded successfully - ID: %s, Name: %s",
                    uploadedFile.getId(), uploadedFile.getFileName());

            return Response.status(Response.Status.CREATED)
                    .entity(uploadedFile)
                    .build();
        }catch (Exception e){
            throw new ApiException(
                    String.format("Error reading file data: %s", e.getMessage()),
                    Response.Status.BAD_REQUEST,
                    "LAM-400-002"
            );
        }

    }


}
