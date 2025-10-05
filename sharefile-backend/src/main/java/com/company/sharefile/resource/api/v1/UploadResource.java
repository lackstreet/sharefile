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
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;

@Path("/api/v1/files")
@Produces(MediaType.APPLICATION_JSON)
public class UploadResource {

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

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
    @RolesAllowed({"sharefile_admin", "sharefile_user"})
    @Operation(summary = "Upload a file", description = "Upload a file with automatic deduplication")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "File uploaded successfully",
                    content = @Content(schema = @Schema(implementation = FileUploadResponseDTO.class))),
            @APIResponse(responseCode = "400", description = "Bad request - missing or invalid parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @APIResponse(responseCode = "413", description = "Payload too large - file size or quota exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @APIResponse(responseCode = "415", description = "Unsupported media type",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @APIResponse(responseCode = "500", description = "Internal server error - upload failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public Response uploadFile(
            @RestForm("file") FileUpload file,
            @RestForm("filename") String filename,
            @RestForm("mimeType") String mimeType,
            @HeaderParam("X-Forwarded-For") String forwardedFor
    ) {
        log.infof("Upload request received: filename=%s, mimeType=%s", filename, mimeType);

        // Validazione parametri
        if (file == null || file.uploadedFile() == null) {
            log.warn("Upload request rejected: no file provided");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "File is required",
                            "No file provided in the request", "FILE-400-001"))
                    .build();
        }

        if (filename == null || filename.isBlank()) {
            log.warn("Upload request rejected: no filename provided");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponseDTO(400, "Filename is required",
                            "Filename cannot be empty", "FILE-400-002"))
                    .build();
        }

        // Validazione dimensione file
        if (file.size() > MAX_FILE_SIZE) {
            log.warnf("Upload request rejected: file too large (%d bytes)", file.size());
            return Response.status(Response.Status.REQUEST_ENTITY_TOO_LARGE)
                    .entity(new ErrorResponseDTO(413, "File too large",
                            String.format("Maximum file size is %d MB", MAX_FILE_SIZE / (1024 * 1024)),
                            "FILE-413-001"))
                    .build();
        }


        log.infof("Processing upload: filename=%s, mimeType=%s, size=%d bytes",
                filename, mimeType, file.size());

        try {
            UserEntity user = userService.getCurrentUser();
            String clientIp = getClientIp(forwardedFor);

            // Upload del file
            try (InputStream fileStream = Files.newInputStream(file.uploadedFile())) {
                FileEntity uploadedFile = fileUploadService.uploadFile(
                        user,
                        filename,
                        mimeType,
                        file.size(),
                        fileStream,
                        clientIp
                );

                log.infof("File uploaded successfully: fileId=%s, user=%s",
                        uploadedFile.getId(), user.getEmail());

                return Response.ok(new FileUploadResponseDTO(
                        uploadedFile.getId(),
                        uploadedFile.getOriginalFileName(),
                        uploadedFile.getStoredFileName(),
                        uploadedFile.getFileSizeBytes(),
                        uploadedFile.getMimeType(),
                        uploadedFile.getChecksumSha256(),
                        uploadedFile.getCreatedAt().toString()
                )).build();
            }

        } catch (Exception e) {
            log.warnf("Upload failed: quota exceeded - %s", e.getMessage());
            return Response.status(Response.Status.REQUEST_ENTITY_TOO_LARGE)
                    .entity(new ErrorResponseDTO(413, "Quota exceeded",
                            e.getMessage(), "FILE-413-002"))
                    .build();

        }
    }

    @DELETE
    @Path("/{fileId}")
    @RolesAllowed({"sharefile_admin", "sharefile_user"})
    @Operation(summary = "Delete a file", description = "Delete a file by its ID")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "File deleted successfully",
                    content = @Content(schema = @Schema(implementation = DeleteResponseDTO.class))),
            @APIResponse(responseCode = "404", description = "File not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @APIResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    public Response deleteFile(@PathParam("fileId") UUID fileId) {
        log.infof("Delete request for file: %s", fileId);

        try {
            UserEntity user = userService.getCurrentUser();
            fileUtils.deleteFile(fileId, user);

            log.infof("File deleted successfully: fileId=%s, user=%s", fileId, user.getEmail());
            return Response.ok(new DeleteResponseDTO("File deleted successfully", fileId)).build();

        } catch (Exception e) {
            log.errorf(e, "Delete failed for fileId=%s: %s", fileId, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponseDTO(500, "Delete failed",
                            e.getMessage(), "FILE-500-003"))
                    .build();
        }
    }

    /**
     * Estrae l'IP reale del client dalla header X-Forwarded-For
     */
    private String getClientIp(String forwardedFor) {
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // Prende il primo IP dalla lista (IP originale del client)
            return forwardedFor.split(",")[0].trim();
        }
        return "unknown";
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