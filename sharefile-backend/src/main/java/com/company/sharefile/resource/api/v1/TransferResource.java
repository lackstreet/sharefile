package com.company.sharefile.resource.api.v1;

import com.company.sharefile.dto.v1.records.request.InitTransferRequestDTO;
import com.company.sharefile.dto.v1.records.response.InitTransferResponseDTO;
import com.company.sharefile.service.TransferService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;


@Path("/api/v1/transfers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransferResource {
    @Inject
    Logger log;

    @Inject
    TransferService transferService;

    @POST
    @Path("/init")
    //@RolesAllowed({"user", "admin"}) // Richiede autenticazione
    public Response initTransfer(@Valid InitTransferRequestDTO request) {
        log.infof("Received init transfer request with %d files", request.files().size());

        InitTransferResponseDTO response = transferService.initialization(request);

        return Response
                .status(Response.Status.CREATED)
                .entity(response)
                .build();
    }


    @GET
    @Path("/{transferId}")
    public Response getTransferStatus(@PathParam("transferId") Long transferId) {
        // TODO: implementare
        return Response.ok().build();
    }

}
