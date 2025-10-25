package com.company.sharefile.service;

import com.company.sharefile.dto.v1.records.QuotaInfo;
import com.company.sharefile.dto.v1.records.UserInfoDTO;
import com.company.sharefile.dto.v1.records.request.UserCreateRequestDTO;
import com.company.sharefile.dto.v1.records.response.UserCreateResponseDTO;
import com.company.sharefile.entity.UserEntity;
import com.company.sharefile.exception.ApiException;
import com.company.sharefile.mapper.UserMapper;
import com.company.sharefile.repository.UserRepository;
import com.company.sharefile.utils.PlanType;
import com.company.sharefile.utils.UserUtils;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.logging.Logger;
import org.apache.commons.text.WordUtils;

import java.time.LocalDateTime;

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

    @Inject
    PlanService planService;


    @Transactional
    protected UserCreateResponseDTO createUserOnKeycloakAndSaveOnLocalDb(UserCreateRequestDTO newUserRequest) {

        String email = newUserRequest.email().toLowerCase().trim();
        String keycloakId = null;

        try {
            keycloakId = keycloakService.createUserInKeycloak(newUserRequest);
            log.infof("User created in Keycloak with ID: %s", keycloakId);

            // 4. CREA RECORD LOCALE
            UserEntity newUser = new UserEntity();
            newUser.setKeycloakId(keycloakId);
            newUser.setEmail(email);
            newUser.setUsername(email);
            newUser.setFirstName(WordUtils.capitalizeFully(newUserRequest.firstName().trim()));
            newUser.setLastName(WordUtils.capitalizeFully(newUserRequest.lastName().trim()));
            newUser.setIsActive(true);
            newUser.setStoragePlan(planService.getPlanByType(PlanType.BASIC));
            newUser.setUsedStorageBytes(0L);

            userRepository.persist(newUser);

            log.infof("User saved in local DB with ID: %s", newUser.getId());
            return userMapper.toCreateResponseDTO(newUser);

        }catch (ApiException e) {
            if (keycloakId != null) {
                log.warnf("Rolling back - deleting user %s from Keycloak", keycloakId);
                keycloakService.deleteUserFromKeycloak(keycloakId);
            }
            log.errorf(e, "Unexpected error creating user with email %s", email);
            throw new ApiException(
                    String.format("Failed to create user with email %s. error: %s", email, e.getMessage()),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-005"
            );
        }
    }

    @Transactional
    public UserCreateResponseDTO create(UserCreateRequestDTO newUserRequest) {
        log.infof("UserService: Creating new user with email: %s", newUserRequest.email());
        String email = newUserRequest.email().toLowerCase().trim();
        // 2. VERIFICA SE L'UTENTE ESISTE GIÀ NEL DB LOCALE
        UserEntity existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            log.warnf("User with email %s already exists in local DB", email);
            throw new ApiException(
                    String.format("User with email %s already exists.", email),
                    Response.Status.CONFLICT,
                    "LAM-409-001"
            );
        }
        return createUserOnKeycloakAndSaveOnLocalDb(newUserRequest);
    }

    @Transactional
    public UserInfoDTO getCurrentUser(){
        OidcJwtCallerPrincipal principal = (OidcJwtCallerPrincipal) securityIdentity.getPrincipal();
        String email  = principal.getClaim("email");
        String keycloakId   = principal.getClaim(Claims.sub.name());
        log.infof("UserService: Getting current user with keycloakId: %s, email: %s", keycloakId, email);

        UserEntity user = findByKeycloakId(keycloakId);
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
        return userMapper.toUserInfoDTO(user);
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

    public boolean hasAvailableQuota(@NotBlank String keycloakId, long requiredBytes){
        UserEntity user = userRepository.findByKeycloakId(keycloakId);
        if(user == null ){
            throw new ApiException(
                    String.format("User not found in local DB for keycloakId: %s", keycloakId),
                    jakarta.ws.rs.core.Response.Status.NOT_FOUND,
                    "LAM-404-002"
            );
        }
        long availableBytes = user.getStoragePlan().getStorageQuotaBytes() - user.getUsedStorageBytes();
        return availableBytes >= requiredBytes;
    }

    public long getAvailableStorage(@NotBlank String keycloakId){
        UserEntity user = userRepository.findByKeycloakId(keycloakId);
        if(user == null ){
            throw new ApiException(
                    String.format("User not found in local DB for keycloakId: %s", keycloakId),
                    jakarta.ws.rs.core.Response.Status.NOT_FOUND,
                    "LAM-404-002"
            );
        }
        return Math.max(0,user.getStoragePlan().getStorageQuotaBytes() - user.getUsedStorageBytes());
    }

    @Transactional
    public void incrementStorageUsed(@NotBlank String keycloakId, long bytesToAdd){
        log.infof("UserService: Incrementing storage used for user with keycloakId: %s, bytesToAdd: %d", keycloakId, bytesToAdd);
        UserEntity user = findByKeycloakId(keycloakId);
        if(user == null ){
            throw new ApiException(
                    String.format("User not found in local DB for keycloakId: %s", keycloakId),
                    Response.Status.NOT_FOUND,
                    "LAM-404-002"
            );
        }

        long newStorageUsed = user.getUsedStorageBytes() + bytesToAdd;
        if(newStorageUsed > user.getStoragePlan().getStorageQuotaBytes()){
            throw new ApiException(
                    String.format("Storage quota exceeded. Used: %d bytes, Available: %d bytes",newStorageUsed, user.getStoragePlan().getStorageQuotaBytes()),
                    Response.Status.BAD_REQUEST,
                    "LAM-400-003"
            );
        }
        user.setUsedStorageBytes(newStorageUsed);
        userRepository.persist(user);
    }

    @Transactional
    public void decrementStorageUsed(@NotBlank String keycloakId, long bytesToSubtract){
        log.infof("UserService: Decrementing storage used for user with keycloakId: %s, bytesToSubtract: %d", keycloakId, bytesToSubtract);
        UserEntity user = userRepository.findByKeycloakId(keycloakId);
        if(user == null ){
            throw new ApiException(
                    String.format("User not found in local DB for keycloakId: %s", keycloakId),
                    Response.Status.NOT_FOUND,
                    "LAM-404-002"
            );
        }

        long newStorageUsed = Math.max(0, user.getUsedStorageBytes() - bytesToSubtract);

        user.setUsedStorageBytes(newStorageUsed);
        userRepository.persist(user);
    }

    public QuotaInfo getCurrentUserQuota(){
        OidcJwtCallerPrincipal principal = (OidcJwtCallerPrincipal) securityIdentity.getPrincipal();
        String keycloakId  = principal.getClaim(Claims.sub.name());
        UserEntity user = findByKeycloakId(keycloakId);
        if(user == null ){
            throw new ApiException(
                    String.format("User not found in local DB for keycloakId: %s", keycloakId),
                    Response.Status.NOT_FOUND,
                    "LAM-404-002"
            );
        }
        return userMapper.toQuotaInfo(user);

    }

    public UserEntity findByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId);
    }


}