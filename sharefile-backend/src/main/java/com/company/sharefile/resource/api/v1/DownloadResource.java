package com.company.sharefile.resource.api.v1;

import com.company.sharefile.entity.FileEntity;
import com.company.sharefile.exception.ApiException;
import com.company.sharefile.service.FileService;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.util.UUID;

@Path("v1/download")
@Authenticated
public class DownloadResource {

    @Inject
    Logger log;

    @Inject
    FileService fileService;

    @Inject
    SecurityIdentity securityIdentity;


    @GET
    @Path("/file/{fileId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("fileId") String fileId){
        log.infof("Received download request for fileId: %s", fileId);

        OidcJwtCallerPrincipal principal = (OidcJwtCallerPrincipal) securityIdentity.getPrincipal();
        String keycloakId = principal.getClaim(Claims.sub.name());

        FileEntity fileEntity = fileService.getFileById(fileId, keycloakId);
        if(fileEntity == null){
            throw new ApiException(
                    String.format("File with ID %s not found", fileId),
                    Response.Status.NOT_FOUND,
                    "LAM-404-001"
            );
        }

        log.infof("Preparing file %s for download", fileEntity.getFileName());

        byte[] decryptedData = fileService.downloadFileAsBytes(fileId, keycloakId);

        return Response.ok(decryptedData)
                .header("Content-Disposition",
                        String.format("attachment; filename=\"%s.%s\"", fileEntity.getFileName(),fileEntity.getMimeType().split("/")[1]))
                .header("Content-Type",
                        fileEntity.getMimeType() != null ?
                                fileEntity.getMimeType() : MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Length", decryptedData.length)
                .header("X-File-ID", fileEntity.getId())
                .header("X-File-Checksum", fileEntity.getChecksum())
                .build();
    }
}
