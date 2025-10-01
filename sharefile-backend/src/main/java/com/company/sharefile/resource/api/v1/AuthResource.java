package com.company.sharefile.resource.api.v1;

import com.company.sharefile.dto.v1.request.AuthenticationRequestDTO;
import com.company.sharefile.dto.v1.response.AuthenticationResponseDTO;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;

import java.util.Map;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    Logger log;

    @ConfigProperty(name = "keycloak.server-url")
    String keycloakServerUrl;

    @ConfigProperty(name = "keycloak.realm")
    String realm;

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret")
    String clientSecret;

    @POST
    @Path("/login")
    @APIResponse(responseCode = "200", description = "Successful login",
    content = @Content (mediaType = MediaType.APPLICATION_JSON,
    schema = @Schema (implementation = AuthenticationResponseDTO.class)))
    @APIResponse(responseCode = "401", description = "Invalid username or password")
    @PermitAll
    public Response login(AuthenticationRequestDTO loginRequest) {

        log.infof("Login attempt for user: %s", loginRequest.getUsername());

        try {
            org.keycloak.admin.client.Keycloak userKeycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm(realm)
                    .username(loginRequest.getUsername())
                    .password(loginRequest.getPassword())
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();

            AccessTokenResponse tokenResponse = userKeycloak.tokenManager().getAccessToken();

            log.infof("User %s logged in successfully", loginRequest.getUsername());

            AuthenticationResponseDTO response = new AuthenticationResponseDTO();
            response.setAccessToken(tokenResponse.getToken());
            response.setRefreshToken(tokenResponse.getRefreshToken());
            response.setExpiresIn(tokenResponse.getExpiresIn());
            response.setTokenType("Bearer");

            return Response.ok(response).build();

        } catch (Exception e) {
            log.errorf("Login failed for user %s: %s", loginRequest.getUsername(), e.getMessage());

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of(
                            "error", "invalid_grant",
                            "error_description", "Invalid username or password"
                    ))
                    .build();
        }
    }

    @POST
    @Path("/refresh")
    @PermitAll
    public Response refreshToken(Map<String, String> request) {

        String refreshToken = request.get("refresh_token");
        log.info("Token refresh attempt");

        try {
            org.keycloak.admin.client.Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .build();

            keycloak.tokenManager().refreshToken();
            AccessTokenResponse tokenResponse = keycloak.tokenManager().getAccessToken();

            AuthenticationResponseDTO response = new AuthenticationResponseDTO();
            response.setAccessToken(tokenResponse.getToken());
            response.setRefreshToken(tokenResponse.getRefreshToken());
            response.setExpiresIn(tokenResponse.getExpiresIn());
            response.setTokenType("Bearer");

            return Response.ok(response).build();

        } catch (Exception e) {
            log.errorf("Token refresh failed: %s", e.getMessage());

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of(
                            "error", "invalid_token",
                            "error_description", "Invalid or expired refresh token"
                    ))
                    .build();
        }
    }
}