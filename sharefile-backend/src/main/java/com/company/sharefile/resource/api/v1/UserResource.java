package com.company.sharefile.resource.api.v1;

import com.company.sharefile.dto.v1.records.QuotaInfo;
import com.company.sharefile.dto.v1.records.UserInfoDTO;
import com.company.sharefile.dto.v1.records.response.ErrorResponseDTO;
import com.company.sharefile.dto.v1.records.request.UserCreateRequestDTO;
import com.company.sharefile.dto.v1.records.response.UserCreateResponseDTO;
import com.company.sharefile.service.UserService;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.logging.Logger;

import java.net.URI;

@PermitAll
@Path("/api/v1/users")
public class UserResource {
    @Inject
    UserService userService;

    @Inject
    Logger log;

    @Path("/create")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new user",
            description = "creates a new user in the system.")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "201",
                    description = "user created successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = UserCreateRequestDTO.class))
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "invalid input data.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @APIResponse(
                    responseCode = "409",
                    description = "Resource conflict (e.g., email already exists).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    public Response createUser(UserCreateRequestDTO newUser) {

        log.infof("Resource: Received request to create user for email: %s", newUser.email());
        UserCreateResponseDTO userCreated = userService.createAccess(newUser);

        URI location = UriBuilder.fromPath("/api/v1/users/{id}")
                .build(userCreated.id());

        log.infof("Resource: Successfully created user with ID: %s. Returning 201.", userCreated.id());

        return Response.created(location)
                .entity(userCreated)
                .build();
    }


    @Path("/me")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get current user info")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Current user info retrieved successfully.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = UserInfoDTO.class))
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @APIResponse(
                    responseCode = "403",
                    description = "Forbidden - Insufficient permissions.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    public Response getCurrentUser() {
        log.info("Getting current user info");

        UserInfoDTO user = userService.getCurrentUser();

        return Response.ok(user).build();
    }

    /**
     * NUOVO: Info quota storage
     */
    @GET
    @Path("/quota")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get storage quota info")
    public Response getQuota() {
        log.info("Getting quota info");
        QuotaInfo quota = userService.getQuotaInfo();
        return Response.ok(quota).build();
    }
}