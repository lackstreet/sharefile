package com.company.sharefile.resource.api.v1;

import com.company.sharefile.dto.v1.response.ErrorResponseDTO;
import com.company.sharefile.dto.v1.request.UserCreateRequestDTO;
import com.company.sharefile.dto.v1.response.UserCreateResponseDTO;
import com.company.sharefile.service.UserService;

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


@Path("/api/v1/users")
public class UserResource {
    @Inject
    UserService userService;

    @Inject
    Logger log;

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
    public Response createUser(UserCreateRequestDTO userRequestDTO) {

        log.infof("Resource: Received request to create user for email: %s", userRequestDTO.getEmail());
        UserCreateResponseDTO userResponseDTO = userService.createUser(userRequestDTO);

        URI location = UriBuilder.fromPath("/api/v1/users/{id}")
                .build(userResponseDTO.getId());

        log.infof("Resource: Successfully created user with ID: %d. Returning 201.", userResponseDTO.getId());

        return Response.created(location)
                .entity(userResponseDTO)
                .build();
    }
}