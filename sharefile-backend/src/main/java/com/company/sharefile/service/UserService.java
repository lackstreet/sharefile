package com.company.sharefile.service;

import com.company.sharefile.dto.v1.records.QuotaInfo;
import com.company.sharefile.dto.v1.request.UserCreateRequestDTO;
import com.company.sharefile.dto.v1.response.UserCreateResponseDTO;
import com.company.sharefile.entity.UserEntity;
import com.company.sharefile.exception.ApiException;
import com.company.sharefile.mapper.UserMapper;
import com.company.sharefile.repository.UserRepository;
import com.company.sharefile.utils.UserUtils;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.apache.commons.text.WordUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;

    @Inject
    Logger log;

    @Inject
    KeycloakService keycloakService;

    @Inject
    UserMapper userMapper;

    @Inject
    SecurityIdentity securityIdentity;


    @Inject
    UserUtils userUtils;


    @Transactional
    public UserCreateResponseDTO createUser(UserCreateRequestDTO userRequestDTO) {
        log.infof("UserService: Creating new user with email: %s", userRequestDTO.getEmail());

        // 1. NORMALIZZA EMAIL
        String normalizedEmail = userRequestDTO.getEmail().toLowerCase().trim();

        // 2. VERIFICA SE L'UTENTE ESISTE GIÀ NEL DB LOCALE
        UserEntity existingUser = userRepository.findByEmail(normalizedEmail);
        if (existingUser != null) {
            log.warnf("User with email %s already exists in local DB", normalizedEmail);
            throw new ApiException(
                    String.format("User with email %s already exists.", normalizedEmail),
                    Response.Status.CONFLICT,
                    "LAM-409-001"
            );
        }

        String keycloakUserId = null;

        try {
            // 3. CREA UTENTE IN KEYCLOAK
            keycloakUserId = keycloakService.createUserInKeycloak(userRequestDTO);
            log.infof("User created in Keycloak with ID: %s", keycloakUserId);

            // 4. CREA RECORD LOCALE
            UserEntity newUser = new UserEntity();
            newUser.setKeycloakId(keycloakUserId);
            newUser.setEmail(normalizedEmail);
            newUser.setUsername(normalizedEmail);
            newUser.setFirstName(WordUtils.capitalizeFully(userRequestDTO.getFirstName().trim()));
            newUser.setLastName(WordUtils.capitalizeFully(userRequestDTO.getLastName().trim()));
            newUser.setIsActive(true);

            // 5. SALVA NEL DB LOCALE
            userRepository.persist(newUser);
            log.infof("User saved in local DB with ID: %s", newUser.getId());

            // 6. CONVERTI E RESTITUISCI DTO
            return userMapper.toCreateResponseDTO(newUser);

        } catch (ApiException e) {
            // ROLLBACK: Se qualcosa fallisce, elimina da Keycloak
            if (keycloakUserId != null) {
                log.warnf("Rolling back - deleting user %s from Keycloak", keycloakUserId);
                keycloakService.deleteUserFromKeycloak(keycloakUserId);
            }
            log.errorf(e, "Unexpected error creating user with email %s", normalizedEmail);
            throw new ApiException(
                    String.format("Failed to create user with email %s. error: %s", normalizedEmail, e.getMessage()),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-005"
            );
        }
    }

    @Transactional
    public UserEntity getCurrentUser(){
        String keycloakId = securityIdentity.getPrincipal().getName();
        log.infof("UserService: Getting current user with keycloakId: %s, email: %s", keycloakId);

        UserEntity user = userRepository.findByKeycloakId(keycloakId);
        if(user == null){
            log.errorf("User not found in local DB for keycloakId: %s", keycloakId);
            throw new ApiException(
                    String.format("User not found in local DB for keycloakId: %s", keycloakId),
                    jakarta.ws.rs.core.Response.Status.NOT_FOUND,
                    "LAM-404-001"
            );

        }
        user.setLastLoginAt(LocalDateTime.now());
        log.infof("User last login updated for keycloakId: %s", keycloakId);
        return user;
    }

    @Transactional
    public boolean hasAvailableQuota(UserEntity user, Long fileSizeByte){
        user = userRepository.findById(user.getId());
        if(user == null){
            log.warnf("User: %s not found in local DB during quota check.", user.getId());
            throw new ApiException(
                    "User not found in local database.",
                    jakarta.ws.rs.core.Response.Status.NOT_FOUND,
                    "LAM-404-001"
            );
        }
        return userUtils.hasAvailableQuota(user,fileSizeByte);
    }

    @Transactional
    public void updateLastLogin(@Email(message = "Invalid email format") @Size(max = 255) @NotBlank(message = "Email is required") String username) {
        UserEntity user = userRepository.findByEmail(username);
        if (user != null) {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.persist(user);
            log.infof("Updated last login for user: %s", username);
        } else {
            log.warnf("User not found for updating last login: %s", username);
        }
    }

    public QuotaInfo getQuotaInfo(UserEntity user) {
        return new QuotaInfo(
                user.getStorageQuotaBytes(),
                user.getUsedStorageBytes(),
                user.getStorageQuotaBytes() - user.getUsedStorageBytes()
        );
    }

    @Transactional
    public void incrementUsedStorage(UUID userId, Long fileSizeBytes) {
        UserEntity user = userRepository.findById(userId);
        if (user != null) {
            long newUsed = user.getUsedStorageBytes() + fileSizeBytes;
            user.setUsedStorageBytes(newUsed);
            log.infof("Storage incremented for user %s: %d -> %d bytes",
                    userId, user.getUsedStorageBytes() - fileSizeBytes, newUsed);
        }
    }

    /**
     * NUOVO: Decrementa storage utilizzato (quando si elimina file)
     */
    @Transactional
    public void decrementUsedStorage(UUID userId, Long fileSizeBytes) {
        UserEntity user = userRepository.findById(userId);
        if (user != null) {
            long newUsed = Math.max(0, user.getUsedStorageBytes() - fileSizeBytes);
            user.setUsedStorageBytes(newUsed);
            log.infof("Storage decremented for user %s: %d -> %d bytes",
                    userId, user.getUsedStorageBytes() + fileSizeBytes, newUsed);
        }
    }
}