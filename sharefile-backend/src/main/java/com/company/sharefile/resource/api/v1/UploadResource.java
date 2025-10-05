package com.company.sharefile.resource.api.v1;

import com.company.sharefile.dto.v1.response.ErrorResponseDTO;
import com.company.sharefile.entity.FileEntity;
import com.company.sharefile.entity.UserEntity;
import com.company.sharefile.service.FileUploadService;
import com.company.sharefile.service.UserService;
import com.company.sharefile.utils.FileUtils;
import jakarta.annotation.security.RolesAllowed;
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
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload; // ← Import corretto

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

@Path("/api/v1/files")
@Produces(MediaType.APPLICATION_JSON)
public class UploadResource {

    @Inject
    Logger log;

    @Inject
    UserService userService;

    @Inject
    FileUploadService fileUploadService;

    @Inject
    FileUtils fileUtils;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"user", "admin"})
    @Operation(summary = "Upload a file", description = "Upload a file with automatic deduplication")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "File uploaded successfully"),
            @APIResponse(responseCode = "413", description = "Quota exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @APIResponse(responseCode = "500", description = "Upload failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public Response uploadFile(
            @RestForm("file") FileUpload file, // ← Nuovo approccio
            @RestForm("filename") String filename,
            @RestForm("mimeType") String mimeType,
            @HeaderParam("X-Forwarded-For") String forwardedFor
    ) throws FileNotFoundException {
        log.infof("Upload request: filename=%s, mimeType=%s, size=%d",
                filename, mimeType, file.size());

        try {
            UserEntity user = userService.getCurrentUser();

            // Leggi file da FileUpload
            InputStream fileStream = new FileInputStream(file.uploadedFile().toFile());

            FileEntity uploadedFile = fileUploadService.uploadFile(
                    user,
                    filename,
                    mimeType,
                    file.size(),
                    fileStream,
                    forwardedFor != null ? forwardedFor : "unknown"
            );

            return Response.ok(new FileUploadResponseDTO(
                    uploadedFile.getId(),
                    uploadedFile.getOriginalFileName(),
                    uploadedFile.getStoredFileName(),
                    uploadedFile.getFileSizeBytes(),
                    uploadedFile.getMimeType(),
                    uploadedFile.getChecksumSha256(),
                    uploadedFile.getCreatedAt().toString()
            )).build();

        } catch (Exception e) {
            log.errorf(e, "Upload error: %s", e.getMessage());
            throw e;
        }
    }

    @DELETE
    @Path("/{fileId}")
    @RolesAllowed({"user", "admin"})
    @Operation(summary = "Delete a file")
    public Response deleteFile(@PathParam("fileId") UUID fileId) {
        log.infof("Delete request for file: %s", fileId);

        UserEntity user = userService.getCurrentUser();
        fileUtils.deleteFile(fileId, user);

        return Response.ok(new DeleteResponseDTO("File deleted", fileId)).build();
    }

    // DTOs
    public record FileUploadResponseDTO(
            UUID fileId,
            String originalFilename,
            String storedFilename,
            Long sizeBytes,
            String mimeType,
            String checksum,
            String uploadedAt
    ) {}

    public record DeleteResponseDTO(String message, UUID fileId) {}
}