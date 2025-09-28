package com.company.sharefile.repository;

import com.company.sharefile.entity.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepository<UserEntity> {

    public UserEntity findByKeycloakId(String keycloakId) {
        return find("keycloakId", keycloakId).firstResult();
    }

    public UserEntity findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public UserEntity findByUsername(String username) {
        return find("username", username).firstResult();
    }

}
