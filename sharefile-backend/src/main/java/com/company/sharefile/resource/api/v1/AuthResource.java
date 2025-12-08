package com.company.sharefile.resource.api.v1;

import com.company.sharefile.dto.v1.records.request.AuthenticationRequestDTO;
import com.company.sharefile.dto.v1.records.request.RefreshTokenRequestDTO;
import com.company.sharefile.dto.v1.records.response.AuthenticationResponseDTO;
import com.company.sharefile.exception.ApiException;
import com.company.sharefile.service.AuthService;
import com.company.sharefile.service.UserService;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.logging.Logger;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Blocking
public class AuthResource {

    @Inject
    Logger log;

    @Inject
    UserService userService;

    @Inject
    AuthService authService;

    @POST
    @Path("/login")
    @PermitAll
    @Operation(
            summary = "Autenticazione utente",
            description = "Esegue il login dell'utente e restituisce access token e refresh token"
    )
    public Response login(@Valid AuthenticationRequestDTO loginRequest) {
        log.infof("Login attempt for user: %s", loginRequest.username());
        try {
            // Ottieni i token di autenticazione da AuthService
            AuthenticationResponseDTO tokens = authService.getAuthentication(loginRequest);
            // Crea i cookie per access token,  refresh token e CSRF token
            String csrf = authService.generateCsrfToken();

            NewCookie accessCookie = new NewCookie.Builder("access_token")
                    .value(tokens.accessToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .comment("JWT Access Token")
                    .maxAge(tokens.expireIn())
                    .sameSite(NewCookie.SameSite.LAX)
                    .build();

            NewCookie refreshCookie = new NewCookie.Builder("refresh_token")
                    .value(tokens.refreshToken())
                    .path("/")
                    .comment("JWT Refresh Token")
                    .maxAge(tokens.refreshExpiresIn())
                    .secure(true)
                    .httpOnly(true)
                    .sameSite(NewCookie.SameSite.STRICT)
                    .build();

            NewCookie csrfCookie = new NewCookie.Builder("csrf_token")
                    .value(csrf)
                    .path("/")
                    .comment("CSRF Token")
                    .maxAge(tokens.refreshExpiresIn())
                    .secure(true)
                    .httpOnly(false)
                    .sameSite(NewCookie.SameSite.LAX)
                    .build();



            userService.updateLastLogin(loginRequest.username());
            log.infof("User %s logged in successfully", loginRequest.username());

            return Response.ok(Map.of("message", "Login successful"))
                    .cookie(accessCookie,refreshCookie, csrfCookie)
                    .build();

        } catch (ApiException e) {
            // Ritorna al frontend lo status e il messaggio dell'ApiException
            return Response.status(e.getStatus())
                    .entity(Map.of("error", e.getMessage(), "code", e.getInternalDocumentationErrorCode()))
                    .build();
        } catch (Exception e) {
            log.errorf(e, "Unexpected error during login for user: %s", loginRequest.username());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Unexpected authentication error"))
                    .build();
        }
    }


    @POST
    @Path("/refresh")
    public Response refresh(@CookieParam("refresh_token") String refreshToken) {
        if (refreshToken == null) {
            log.warn("Refresh token is null");
            return Response.status(401).entity(Map.of("message:","refreshToken null")).build();
        }
        try {

            AuthenticationResponseDTO tokens = authService.refreshToken(refreshToken);

            NewCookie accessCookie = new NewCookie.Builder("access_token")
                    .value(tokens.accessToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .comment("JWT Access Token")
                    .maxAge(tokens.expireIn())
                    .sameSite(NewCookie.SameSite.LAX)
                    .build();

            return Response.ok().cookie(accessCookie).build();

        }catch (ApiException e) {
            return Response.status(e.getStatus())
                    .entity(Map.of("error", e.getMessage(), "code", e.getInternalDocumentationErrorCode()))
                    .build();
        } catch (Exception e) {
            log.error("Unexpected error during token refresh", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Unexpected token refresh error"))
                    .build();
        }
    }

    @POST
    @Path("/logout")
    @Authenticated
    @Operation(
            summary = "Disconnessione utente",
            description = "Effettua il logout dell'utente invalidando la sessione e revocando i token su Keycloak"
    )
    public Response logout(RefreshTokenRequestDTO token) {
        log.info("Logout attempt");

        authService.logout(token);

        return Response.ok(Map.of("message", "Logged out successfully")).build();
    }
}