package com.company.sharefile.service;

import com.company.sharefile.dto.v1.request.UserCreateRequestDTO;
import com.company.sharefile.dto.v1.response.UserCreateResponseDTO;
import com.company.sharefile.entity.UserEntity;
import com.company.sharefile.exception.ApiException;
import com.company.sharefile.mapper.UserMapper;
import com.company.sharefile.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.apache.commons.text.WordUtils;

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
}