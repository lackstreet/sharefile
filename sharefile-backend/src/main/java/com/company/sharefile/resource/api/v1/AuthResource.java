package com.company.sharefile.resource.api.v1;

import com.company.sharefile.dto.v1.request.AuthenticationRequestDTO;
import com.company.sharefile.dto.v1.request.RefreshTokenRequestDTO;
import com.company.sharefile.dto.v1.response.AuthenticationResponseDTO;
import com.company.sharefile.service.UserService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import com.company.sharefile.client.KeycloakTokenClient;

import java.util.Map;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Blocking
public class AuthResource {

    @Inject
    Logger log;

    @Inject
    @RestClient
    KeycloakTokenClient tokenClient;

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret")
    String clientSecret;

    @Inject
    SecurityIdentity identity;

    @Inject
    UserService userService;

    @POST
    @Path("/login")
    @PermitAll
    public Response login(AuthenticationRequestDTO loginRequest) {
        log.infof("Login attempt for user: %s", loginRequest.getUsername());

        try {
            MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
            formData.putSingle("grant_type", "password");
            formData.putSingle("client_id", clientId);
            formData.putSingle("client_secret", clientSecret);
            formData.putSingle("username", loginRequest.getUsername());
            formData.putSingle("password", loginRequest.getPassword());

            Map<String, Object> tokenResponse = tokenClient.getToken(formData);

            AuthenticationResponseDTO response = new AuthenticationResponseDTO();
            response.setAccessToken((String) tokenResponse.get("access_token"));
            response.setRefreshToken((String) tokenResponse.get("refresh_token"));
            response.setExpiresIn((long) ((Number) tokenResponse.get("expires_in")).intValue());
            response.setTokenType("Bearer");


            userService.updateLastLogin(loginRequest.getUsername());
            log.infof("User %s logged in successfully", loginRequest.getUsername());
            return Response.ok(response).build();

        } catch (ClientWebApplicationException e) {
            log.errorf("Login failed for user %s: %s", loginRequest.getUsername(), e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of(
                            "error", "invalid_grant",
                            "error_description", "Invalid username or password"
                    ))
                    .build();
        } catch (Exception e) {
            log.errorf(e, "Login error for user %s", loginRequest.getUsername());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                            "error", "server_error",
                            "error_description", "Authentication service error"
                    ))
                    .build();
        }
    }

    @POST
    @Path("/refresh")
    @PermitAll
    public Response refreshToken(RefreshTokenRequestDTO request) {
        log.info("Token refresh attempt");

        try {
            MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
            formData.putSingle("grant_type", "refresh_token");
            formData.putSingle("client_id", clientId);
            formData.putSingle("client_secret", clientSecret);
            formData.putSingle("refresh_token", request.getRefreshToken());

            Map<String, Object> tokenResponse = tokenClient.getToken(formData);

            AuthenticationResponseDTO response = new AuthenticationResponseDTO();
            response.setAccessToken((String) tokenResponse.get("access_token"));
            response.setRefreshToken((String) tokenResponse.get("refresh_token"));
            response.setExpiresIn((long) ((Number) tokenResponse.get("expires_in")).intValue());
            response.setTokenType("Bearer");

            log.info("Token refreshed successfully");
            return Response.ok(response).build();

        } catch (ClientWebApplicationException e) {
            log.errorf("Token refresh failed: %s", e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of(
                            "error", "invalid_token",
                            "error_description", "Invalid or expired refresh token"
                    ))
                    .build();
        } catch (Exception e) {
            log.errorf(e, "Token refresh error");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                            "error", "server_error",
                            "error_description", "Token refresh failed"
                    ))
                    .build();
        }
    }

    @POST
    @Path("/logout")
    @Authenticated
    public Response logout(RefreshTokenRequestDTO request) {
        log.info("Logout attempt");

        try {
            MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
            formData.putSingle("client_id", clientId);
            formData.putSingle("client_secret", clientSecret);
            formData.putSingle("refresh_token", request.getRefreshToken());

            tokenClient.logout(formData);

            String username = identity.getPrincipal().getName();
            log.infof("Logout requested by user: %s", username);

            log.info("User logged out successfully");
            return Response.ok(Map.of("message", "Logged out successfully")).build();

        } catch (Exception e) {
            log.errorf(e, "Logout error");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "server_error"))
                    .build();
        }
    }
}