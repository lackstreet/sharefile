package com.company.sharefile.service;

import com.company.sharefile.client.KeycloakTokenClient;
import com.company.sharefile.dto.v1.records.request.AuthenticationRequestDTO;
import com.company.sharefile.dto.v1.records.request.RefreshTokenRequestDTO;
import com.company.sharefile.dto.v1.records.response.AuthenticationResponseDTO;
import com.company.sharefile.exception.ApiException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import java.security.SecureRandom;
import java.util.Base64;

@ApplicationScoped
public class AuthService {

    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final String GRANT_TYPE_REFRESH = "refresh_token";
    private static final String PARAM_GRANT_TYPE = "grant_type";
    private static final String PARAM_CLIENT_ID = "client_id";
    private static final String PARAM_CLIENT_SECRET = "client_secret";
    private static final String PARAM_USERNAME = "username";
    private static final String PARAM_PASSWORD = "password";
    private static final String PARAM_REFRESH_TOKEN = "refresh_token";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret")
    String clientSecret;

    @Inject
    @RestClient
    KeycloakTokenClient tokenClient;

    @Inject
    SecurityIdentity identity;

    @Inject
    CsrfService csrfService;

    @Inject
    Logger log;

    private MultivaluedMap<String, String> getLoginFormData(String username, String password) {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle(PARAM_GRANT_TYPE, GRANT_TYPE_PASSWORD);
        formData.putSingle(PARAM_CLIENT_ID, clientId);
        formData.putSingle(PARAM_CLIENT_SECRET, clientSecret);
        formData.putSingle(PARAM_USERNAME, username);
        formData.putSingle(PARAM_PASSWORD, password);
        return formData;
    }

    private MultivaluedMap<String, String> getTokenRefreshFormData(String refreshToken) {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle(PARAM_GRANT_TYPE, GRANT_TYPE_REFRESH);
        formData.putSingle(PARAM_CLIENT_ID, clientId);
        formData.putSingle(PARAM_CLIENT_SECRET, clientSecret);
        formData.putSingle(PARAM_REFRESH_TOKEN, refreshToken);
        return formData;
    }

    private MultivaluedMap<String, String> getLogoutFormData(String refreshToken) {
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.putSingle(PARAM_CLIENT_ID, clientId);
        formData.putSingle(PARAM_CLIENT_SECRET, clientSecret);
        formData.putSingle(PARAM_REFRESH_TOKEN, refreshToken);
        return formData;
    }

    private String getCurrentUsername() {
        return identity != null && identity.getPrincipal() != null
                ? identity.getPrincipal().getName()
                : "unknown";
    }

    public AuthenticationResponseDTO getAuthentication(AuthenticationRequestDTO authenticationRequest) {
        try {
            MultivaluedMap<String, String> loginForm = getLoginFormData(
                    authenticationRequest.username(),
                    authenticationRequest.password()
            );
            return tokenClient.getToken(loginForm);
        } catch (ClientWebApplicationException e) {
            int status = e.getResponse().getStatus();
            if (status == 401) {
                // Qui puoi distinguere tra username e password se il backend lo fornisce
                log.warnf("Invalid password for user: %s", authenticationRequest.username());
                throw new ApiException(
                        "Invalid password.",
                        Response.Status.UNAUTHORIZED,
                        "LAM-401-002"
                );
            } else if (status == 404) {
                log.warnf("User not found: %s", authenticationRequest.username());
                throw new ApiException(
                        "Invalid username.",
                        Response.Status.UNAUTHORIZED,
                        "LAM-401-003"
                );
            } else {
                log.errorf(e, "Authentication service error for user: %s", authenticationRequest.username());
                throw new ApiException(
                        "Authentication service error.",
                        Response.Status.INTERNAL_SERVER_ERROR,
                        "LAM-500-001"
                );
            }
        } catch (Exception e) {
            log.errorf(e, "Authentication service error for user: %s", authenticationRequest.username());
            throw new ApiException(
                    "Authentication service error.",
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-001"
            );
        }
    }


    public AuthenticationResponseDTO refreshToken(String refreshToken) {
        try {
            log.infof("Token refresh attempt for user: %s", getCurrentUsername());
            MultivaluedMap<String, String> refreshTokenForm = getTokenRefreshFormData(refreshToken);
            return tokenClient.getToken(refreshTokenForm);
        } catch (ClientWebApplicationException e) {
            throw new ApiException(
                    "Invalid or expired refresh token.",
                    Response.Status.UNAUTHORIZED,
                    "LAM-401-002"
            );
        } catch (Exception e) {
            throw new ApiException(
                    "Token refresh service error.",
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-002"
            );
        }
    }


    public void logout(RefreshTokenRequestDTO logoutRequest) {
        try {
            String username = getCurrentUsername();
            log.infof("Logout attempt for user: %s", username);

            MultivaluedMap<String, String> logoutForm = getLogoutFormData(logoutRequest.refreshToken());
            tokenClient.logout(logoutForm);

            log.infof("User %s logged out successfully", username);
        } catch (Exception e) {
            log.errorf(e, "Logout service error for user: %s", getCurrentUsername());
            throw new ApiException(
                    "Logout service error.",
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-003"
            );
        }
    }

    public String setCookie(String name, String value, int maxAge, boolean httpOnly){
        String cookie = "%s=%s; Path=/; Max-Age=%d; Secure; SameSite=None".formatted(name, value, maxAge);
        if (httpOnly) cookie += "; HttpOnly";
        return cookie;
    }
    public String generateCsrfToken() {
       return csrfService.generate();
    }
}
