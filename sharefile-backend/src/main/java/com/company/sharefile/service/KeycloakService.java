package com.company.sharefile.service;

import com.company.sharefile.dto.v1.records.request.UserCreateRequestDTO;
import com.company.sharefile.exception.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

@ApplicationScoped
public class KeycloakService {

    @Inject
    Logger log;

    @Inject
    Keycloak keycloak;

    @ConfigProperty(name = "keycloak.realm")
    String keycloakRealm;

    @ConfigProperty(name = "keycloak.email.verification.enabled")
    boolean emailVerificationEnabled;


    public String createUserInKeycloak(UserCreateRequestDTO userRequestDTO) {
        Response response = null;
        String keycloakId="";

        try {
            log.infof("Creating user in Keycloak: %s", userRequestDTO.email());

            // 1. OTTIENI RISORSE KEYCLOAK
            RealmResource realmResource = keycloak.realm(keycloakRealm);
            UsersResource usersResource = realmResource.users();

            // 2. NORMALIZZA EMAIL
            String normalizedEmail = userRequestDTO.email().toLowerCase().trim();

            // 3. CONTROLLA SE UTENTE ESISTE GIÀ IN KEYCLOAK
            List<UserRepresentation> existingUsers = usersResource.search(normalizedEmail, true);

            if (!existingUsers.isEmpty()) {
                log.errorf("User with email %s already exists in Keycloak", normalizedEmail);
                throw new ApiException(
                        "User already exists in Keycloak.",
                        Response.Status.CONFLICT,
                        "LAM-409-002"
                );
            }

            // 4. PREPARA USER REPRESENTATION
            UserRepresentation userRep = new UserRepresentation();
            userRep.setUsername(normalizedEmail);
            userRep.setEmail(normalizedEmail);
            userRep.setFirstName(userRequestDTO.firstName().trim());
            userRep.setLastName(userRequestDTO.lastName().trim());
            userRep.setEnabled(true);
            userRep.setEmailVerified(!emailVerificationEnabled);

            // 5. CREA UTENTE IN KEYCLOAK
            response = usersResource.create(userRep);

            if (response.getStatus() != 201) {
                String error = String.format("Failed to create user in Keycloak. Status: %d", response.getStatus());
                log.errorf("Keycloak user creation failed: %s", error);
                throw new ApiException(
                        error,
                        Response.Status.BAD_GATEWAY,
                        "LAM-502-001"
                );
            }

            // 6. ESTRAI USER ID
            keycloakId = extractUserIdFromResponse(response);
            log.infof("Keycloak user created successfully with ID: %s", keycloakId);

            // 7. IMPOSTA PASSWORD
            setPassword(usersResource, keycloakId, userRequestDTO.password());

            // 8. ASSEGNA GRUPPO DI BASE ("user")
            assignGroup(usersResource, keycloakId);

            // 8. INVIA EMAIL DI VERIFICA (solo se abilitato)
            if (emailVerificationEnabled) {
                sendVerificationEmail(usersResource, keycloakId);
            }

            return keycloakId;

        } catch (Exception e) {
            if( !keycloakId.isBlank())
                deleteUserFromKeycloak(keycloakId);
            log.errorf(e, "Unexpected error creating user in Keycloak: %s", e.getMessage());
            throw new ApiException(
                    "Failed to create user in Keycloak: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-002"
            );
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    log.warn("Failed to close Keycloak response", e);
                }
            }
        }
    }

    /**
     * Elimina un utente da Keycloak (utile per rollback)
     */
    public void deleteUserFromKeycloak(String keycloakId) {
        try {
            log.infof("Deleting user from Keycloak: %s", keycloakId);
            RealmResource realmResource = keycloak.realm(keycloakRealm);
            realmResource.users().get(keycloakId).remove();
            log.infof("User %s deleted from Keycloak", keycloakId);
        } catch (Exception e) {
            log.errorf(e, "Failed to delete user %s from Keycloak", keycloakId);
        }
    }

    // Aggiungi questo metodo in KeycloakService
    private void assignGroup(UsersResource usersResource, String userId) {
        try {
            RealmResource realmResource = keycloak.realm(keycloakRealm);

            // Find the "Users" group
            GroupRepresentation usersGroup = realmResource.groups()
                    .groups("Users", 0, 1)  // Search by name
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Group 'Users' not found"));

            // Assign the group to the user
            usersResource.get(userId).joinGroup(usersGroup.getId());

            log.infof("Group 'Users' assigned to user: %s", userId);

        } catch (Exception e) {
            log.errorf(e, "Failed to assign group to user %s", userId);
            throw new ApiException(
                    String.format("Failed to assign group to user %s in Keycloak. user deleted", userId),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-006"
            );
        }
    }

    private String extractUserIdFromResponse(Response response) {
        String locationHeader = response.getHeaderString("Location");
        if (locationHeader == null || locationHeader.isEmpty()) {
            log.error("Location header missing in Keycloak response");
            throw new ApiException(
                    "Could not retrieve user ID from Keycloak response.",
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-001"
            );
        }

        try {
            return locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
        } catch (Exception e) {
            log.errorf("Failed to extract user ID from location header: %s", locationHeader);
            throw new ApiException(
                    "Invalid Keycloak response format.",
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-003"
            );
        }
    }

    private void setPassword(UsersResource usersResource, String userId, String password) {
        try {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);

            usersResource.get(userId).resetPassword(credential);
            log.infof("Password set successfully for Keycloak user: %s", userId);

        } catch (Exception e) {
            log.errorf(e, "Failed to set password for user %s", userId);
            throw new ApiException(
                    "Failed to set user password in Keycloak.",
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-004"
            );
        }
    }

    private void sendVerificationEmail(UsersResource usersResource, String userId) {
        try {
            usersResource.get(userId).sendVerifyEmail();
            log.infof("Verification email sent for Keycloak user: %s", userId);

        } catch (Exception e) {
            log.warnf(e, "Failed to send verification email for user %s: %s", userId, e.getMessage());
        }
    }

}