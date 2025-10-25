package com.company.sharefile.resource.api.v1;

import com.company.sharefile.dto.v1.records.QuotaInfo;
import com.company.sharefile.service.UserService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1/users/quota")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "User quota", description = "User quota operations")
@Authenticated
public class UserQuotaResource {
    @Inject
    UserService userService;

    @GET
    @Operation(
            summary = "Ottieni quota storage",
            description = "Restituisce le informazioni sulla quota storage dell'utente autenticato"
    )
    public Response getCurrentUserQuota() {
        QuotaInfo quota = userService.getCurrentUserQuota();
        return Response.ok(quota).build();
    }
}
