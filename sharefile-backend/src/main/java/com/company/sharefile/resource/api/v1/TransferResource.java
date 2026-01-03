package com.company.sharefile.resource.api.v1;

import com.company.sharefile.dto.v1.records.request.TransferRequestDTO;
import com.company.sharefile.dto.v1.records.response.FileDataResponseDTO;
import com.company.sharefile.dto.v1.records.response.TransferResponseDTO;
import com.company.sharefile.entity.FileEntity;
import com.company.sharefile.entity.TransferEntity;
import com.company.sharefile.exception.ApiException;
import com.company.sharefile.service.FileService;
import com.company.sharefile.service.TransferService;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.logging.Logger;

@Path("v1/transfers")
@Authenticated
public class TransferResource {
    @Inject
    Logger log;

    @Inject
    TransferService transferService;

    @Inject
    SecurityIdentity securityIdentity;
    @Inject
    FileService fileService;

    @POST
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTransfer(@Valid TransferRequestDTO request)
    {
        log.infof("Received transfer request: %s", request);

        try{
            OidcJwtCallerPrincipal principal = (OidcJwtCallerPrincipal) securityIdentity.getPrincipal();
            String keycloakId = principal.getClaim(Claims.sub.name());

            TransferResponseDTO transfer = transferService.createTransfer(request,keycloakId);
            log.infof("Transfer created successfully with id: %s", transfer.id());
            return Response.status(Response.Status.CREATED).entity(transfer).build();

        }catch(Exception e){
            throw new ApiException(
                    String.format("Error creating transfer: %s", e.getMessage()),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-001"
            );
        }
    }


    @GET
    @Path("/{shareLink}/download/{accessToken}")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, "application/zip"})
    @PermitAll
    public Response downloadTransfer(
        @PathParam("shareLink") String shareLink,
        @PathParam("accessToken") String accessToken,
        @QueryParam("email") String recipientEmail)
    {

        log.infof("Received download request for shareLink: %s", shareLink);
        try{
            FileDataResponseDTO fileData = transferService.download(shareLink, recipientEmail,accessToken);
            return Response.ok(fileData.fileData())
                    .header("Content-Disposition",
                            String.format("attachment; filename=\"%s\"", fileData.fileName()))
                    .header("Content-Type", fileData.mimeType())
                    .header("Content-Length", fileData.fileSize())
                    .build();
        }catch(Exception e){
            throw new ApiException(
                    String.format("Error downloading from shareLink: %s", shareLink),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-002"
            );
        }
    }





}
