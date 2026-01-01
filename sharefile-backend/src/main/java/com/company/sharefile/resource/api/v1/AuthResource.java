package com.company.sharefile.resource.api.v1;

import com.company.sharefile.dto.v1.records.UserInfo;
import com.company.sharefile.exception.ApiException;
import io.quarkus.oidc.IdToken;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.logging.Logger;

import java.net.URI;


@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Blocking
public class AuthResource {

    @Inject
    Logger log;

    @Inject
    @IdToken
    JsonWebToken jwt;

    @Inject
    SecurityIdentity identity;

    @GET
    @Path("/user")
    @Authenticated
    @Operation(
            operationId = "getCurrentUser",
            summary = "Retrieve current authenticated user information",
            description = "Fetches details of the currently authenticated user based on the JWT token."
    )
    public UserInfo getCurrentUser() {
        String subject = jwt.getClaim("sub");
        log.debugf("Get user: %s info", subject);
        try {
            return UserInfo.builder()
                    .username(jwt.getClaim("preferred_username"))
                    .email(jwt.getClaim("email"))
                    .name(jwt.getClaim("given_name"))
                    .roles(identity.getRoles())
                    .build();
        }catch (Exception e){
            throw new ApiException(
                    String.format("Error retrieving user info: %s", e.getMessage()),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-002"
            );
        }
    }


    @POST
    @Path("/logout")
    @Authenticated
    public Response logout() {
        return Response.seeOther(URI.create("/")).build();
    }
}