package com.company.sharefile.resource.api.v1;

import com.company.sharefile.dto.v1.records.request.AuthenticationRequestDTO;
import com.company.sharefile.dto.v1.records.request.RefreshTokenRequestDTO;
import com.company.sharefile.dto.v1.records.response.AuthenticationResponseDTO;
import com.company.sharefile.service.AuthService;
import com.company.sharefile.service.UserService;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.logging.Logger;
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

        AuthenticationResponseDTO authenticate = authService.getAuthentication(loginRequest);
        userService.updateLastLogin(loginRequest.username());

        log.infof("User %s logged in successfully", loginRequest.username());
        return Response.ok(authenticate).build();
    }

    @POST
    @Path("/refresh")
    @PermitAll
    @Operation(
            summary = "Aggiornamento token di accesso",
            description = "Rinnova l'access token utilizzando un refresh token valido"
    )
    public Response refreshToken(RefreshTokenRequestDTO loggedUserToken) {
        log.info("Token refresh attempt for refresh");

        AuthenticationResponseDTO newToken = authService.refreshToken(loggedUserToken);

        log.info("Token refreshed successfully");
        return Response.ok(newToken).build();
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