package com.company.sharefile.service;

import com.company.sharefile.config.ValidationConstants;
import com.company.sharefile.dto.v1.UserDTO;
import com.company.sharefile.entity.UserEntity;
import com.company.sharefile.exception.ApiException;
import com.company.sharefile.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.LocalDateTime;
import java.util.List;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response.Status;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    Logger log;

    // Configurazioni Keycloak da application.properties
    @ConfigProperty(name = "keycloak.server-url")
    String keycloakServerUrl;

    @ConfigProperty(name = "keycloak.realm")
    String keycloakRealm;

    @ConfigProperty(name = "keycloak.admin.username")
    String keycloakAdminUsername;

    @ConfigProperty(name = "keycloak.admin.password")
    String keycloakAdminPassword;

    @ConfigProperty(name = "keycloak.admin.client-id", defaultValue = "admin-cli")
    String keycloakAdminClientId;

    @ConfigProperty(name = "keycloak.email.verification.enabled", defaultValue = "true")
    boolean emailVerificationEnabled;

    @Transactional
    public UserDTO createUser(UserDTO userRequestDTO) {

        log.infof("UserService: Creating new user with email: %s", userRequestDTO.getEmail());

        validateUserData(userRequestDTO);

        UserEntity existingUser = userRepository.findByEmail(userRequestDTO.getEmail());
        if (existingUser != null) {
            throw new ApiException(
                    String.format("User with email %s already exists.", userRequestDTO.getEmail()),
                    Response.Status.CONFLICT,
                    "LAM-409-001"
            );
        }

        String keycloakUserId = null;
        UserEntity newUser = null;

        try {
            // 3. CREA UTENTE IN KEYCLOAK
            keycloakUserId = createUserInKeycloak(userRequestDTO);
            log.infof("UserService: User created in Keycloak with ID: %s", keycloakUserId);

            // 4. CREA RECORD LOCALE (SOLO DATI BUSINESS)
            newUser = new UserEntity();
            newUser.setKeycloakId(keycloakUserId);
            newUser.setEmail(userRequestDTO.getEmail().toLowerCase().trim());
            newUser.setFirstName(userRequestDTO.getFirstName().trim());
            newUser.setLastName(userRequestDTO.getLastName().trim());
            newUser.setActive(true);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setUpdatedAt(LocalDateTime.now());

            // 5. SALVA NEL DB LOCALE
            userRepository.persist(newUser);
            log.infof("UserService: User saved in local DB with ID: %d", newUser.getId());

            // 6. CONVERTI E RESTITUISCI DTO
            return convertToUserDTO(newUser);

        } catch (ApiException e) {
            // Errore specifico di Keycloak
            log.errorf("UserService: Keycloak error creating user %s: %s", userRequestDTO.getEmail(), e.getMessage());
            throw new ApiException(
                    e.getMessage(),
                    e.getStatus(),
                    e.getInternalDocumentationErrorCode()
            );

        } catch (Exception e) {
            // Errore generico - cleanup se necessario
            log.errorf("UserService: Unexpected error creating user %s: %s", userRequestDTO.getEmail(), e.getMessage());

            // CLEANUP: Se il DB locale è stato creato ma c'è stato un errore, rimuovi l'utente
            if (newUser != null && newUser.getId() != null) {
                try {
                    userRepository.delete(newUser);
                    log.debugf("UserService: Cleaned up local user record for: %s", userRequestDTO.getEmail());
                } catch (Exception cleanupEx) {
                    log.errorf("UserService: Failed to cleanup local user: %s", cleanupEx.getMessage());
                }
            }

            // CLEANUP: Se Keycloak user è stato creato ma DB locale ha fallito, rimuovi da Keycloak
            if (keycloakUserId != null) {
                try {
                    deleteUserFromKeycloak(keycloakUserId);
                    log.debugf("UserService: Cleaned up Keycloak user for: %s", userRequestDTO.getEmail());
                } catch (Exception cleanupEx) {
                    log.errorf("UserService: Failed to cleanup Keycloak user: %s", cleanupEx.getMessage());
                }
            }

            throw new ApiException(
                    "Internal server error occurred while creating user.",
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-001"
            );
        }
    }

    private String createUserInKeycloak(UserDTO userRequestDTO) throws ApiException {
        Keycloak keycloak = null;

        try {
            // 1. CONNESSIONE A KEYCLOAK
            keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm("master") // Per autenticazione admin
                    .username(keycloakAdminUsername)
                    .password(keycloakAdminPassword)
                    .clientId(keycloakAdminClientId)
                    .build();

            RealmResource realmResource = keycloak.realm(keycloakRealm);
            UsersResource usersResource = realmResource.users();

            // 2. CONTROLLA SE UTENTE ESISTE GIÀ IN KEYCLOAK
            List<UserRepresentation> existingUsers = usersResource.search(userRequestDTO.getEmail(), true);
            if (!existingUsers.isEmpty()) {
                throw new ApiException(
                        "User already exists in Keycloak.",
                        Response.Status.CONFLICT,
                        "LAM-409-002"
                );
            }

            // 3. PREPARA USER REPRESENTATION
            UserRepresentation userRep = new UserRepresentation();
            userRep.setUsername(userRequestDTO.getEmail());
            userRep.setEmail(userRequestDTO.getEmail());
            userRep.setFirstName(userRequestDTO.getFirstName());
            userRep.setLastName(userRequestDTO.getLastName());
            userRep.setEnabled(true);
            userRep.setEmailVerified(false); // L'utente dovrà verificare l'email

            // 4. CREA UTENTE IN KEYCLOAK
            Response response = usersResource.create(userRep);

            if (response.getStatus() != 201) {
                String error = String.format("Failed to create user in Keycloak. Status: %d", response.getStatus());
                throw new ApiException(
                        error,
                        Response.Status.fromStatusCode(response.getStatus()),
                        "LAM-502-001"
                );
            }

            // 5. ESTRAI USER ID dalla Location header
            String locationHeader = response.getHeaderString("Location");
            if (locationHeader == null) {
                throw new ApiException(
                        "Could not retrieve user ID from Keycloak response.",
                        Response.Status.INTERNAL_SERVER_ERROR,
                        "LAM-500-001"
                );
            }

            String keycloakUserId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

            // 6. IMPOSTA PASSWORD TEMPORANEA
            setTemporaryPassword(usersResource, keycloakUserId, userRequestDTO.getPassword());

            // 7. INVIA EMAIL DI VERIFICA (opzionale)
            sendVerificationEmail(usersResource, keycloakUserId);

            response.close();
            return keycloakUserId;

        } catch (KeycloakUserCreationException e) {
            throw e; // Re-throw eccezioni specifiche

        } catch (ClientErrorException e) {
            // Errori HTTP di Keycloak (4xx)
            throw new ApiException(
                    String.format("Keycloak client error: %s", e.getMessage()),
                    Response.Status.fromStatusCode(e.getResponse().getStatus()),
                    "LAM-400-005"
            );

        } catch (Exception e) {
            // Errori generici (connessione, parsing, etc.)
            throw new ApiException(
                    String.format("Failed to connect to Keycloak: %s", e.getMessage()),
                    Response.Status.SERVICE_UNAVAILABLE,
                    "LAM-503-001"
            );

        } finally {
            if (keycloak != null) {
                try {
                    keycloak.close();
                } catch (Exception e) {
                    log.warnf("Failed to close Keycloak connection: %s", e.getMessage());
                }
            }
        }
    }

    private void setTemporaryPassword(UsersResource usersResource, String userId, String password) {
        try {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(true); // L'utente dovrà cambiarla al primo login

            usersResource.get(userId).resetPassword(credential);
            log.debugf("UserService: Temporary password set for Keycloak user: %s", userId);

        } catch (Exception e) {
            log.warnf("UserService: Failed to set temporary password for user %s: %s", userId, e.getMessage());
            // Non bloccare la creazione per questo errore - l'admin può impostare la password dopo
        }
    }

    private void sendVerificationEmail(UsersResource usersResource, String userId) {
        try {
            usersResource.get(userId).sendVerifyEmail();
            log.debugf("UserService: Verification email sent for Keycloak user: %s", userId);

        } catch (Exception e) {
            log.warnf("UserService: Failed to send verification email for user %s: %s", userId, e.getMessage());
        }
    }

    private void deleteUserFromKeycloak(String keycloakUserId) throws Exception {
        Keycloak keycloak = null;
        try {
            keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm("master")
                    .username(keycloakAdminUsername)
                    .password(keycloakAdminPassword)
                    .clientId(keycloakAdminClientId)
                    .build();

            keycloak.realm(keycloakRealm).users().delete(keycloakUserId);

        } finally {
            if (keycloak != null) {
                keycloak.close();
            }
        }
    }

    private void validateUserData(@NotNull @org.jetbrains.annotations.NotNull UserDTO userRequestDTO) {
        // Email validation
        if (userRequestDTO.getEmail() == null || userRequestDTO.getEmail().isBlank()) {
            throw new ApiException("Email is required.", Response.Status.BAD_REQUEST, "LAM-400-001");
        }

        if (!ValidationConstants.EMAIL_PATTERN.matches(userRequestDTO.getEmail())) {
            throw new ApiException("Invalid email format.", Response.Status.BAD_REQUEST, "LAM-400-002");
        }

        // Password validation (Keycloak la gestirà, ma validazione preventiva)
        if (userRequestDTO.getPassword() == null || userRequestDTO.getPassword().isBlank()) {
            throw new ApiException("Password is required.", Response.Status.BAD_REQUEST, "LAM-400-003");
        }

        if (!ValidationConstants.PASSWORD_PATTERN.matches(userRequestDTO.getPassword())) {
            throw new ApiException("Password does not meet complexity requirements.", Response.Status.BAD_REQUEST, "LAM-400-004");
        }

        // Name validation
        if (userRequestDTO.getFirstName() == null || userRequestDTO.getFirstName().isBlank()) {
            throw new ApiException("First name is required.", Response.Status.BAD_REQUEST, "LAM-400-005");
        }

        if (userRequestDTO.getLastName() == null || userRequestDTO.getLastName().isBlank()) {
            throw new ApiException("Last name is required.", Response.Status.BAD_REQUEST, "LAM-400-006");
        }
    }

    private UserDTO convertToUserDTO(UserEntity userEntity) {
        UserDTO dto = new UserDTO();
        dto.setId(userEntity.getId());
        dto.setKeycloakId(userEntity.getKeycloakId());
        dto.setEmail(userEntity.getEmail());
        dto.setFirstName(userEntity.getFirstName());
        dto.setLastName(userEntity.getLastName());
        dto.setActive(userEntity.isActive());
        dto.setCreatedAt(userEntity.getCreatedAt());
        dto.setUpdatedAt(userEntity.getUpdatedAt());
        // NO password nel DTO di risposta
        return dto;
    }
}

