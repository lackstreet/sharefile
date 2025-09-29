package com.company.sharefile.config;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

@ApplicationScoped
public class KeycloakProducer {

    @Inject
    Logger log;

    @ConfigProperty(name = "keycloak.server-url")
    String keycloakServerUrl;

    @ConfigProperty(name = "keycloak.admin.username")
    String keycloakAdminUsername;

    @ConfigProperty(name = "keycloak.admin.password")
    String keycloakAdminPassword;

    @ConfigProperty(name = "keycloak.admin.client-id", defaultValue = "admin-cli")
    String keycloakAdminClientId;

    private Keycloak keycloak;

    @Produces
    @Singleton
    public Keycloak keycloak() {
        if (keycloak == null) {
            log.info("Initializing Keycloak Admin Client...");
            keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm("master") // Realm per autenticazione admin
                    .username(keycloakAdminUsername)
                    .password(keycloakAdminPassword)
                    .clientId(keycloakAdminClientId)
                    .build();
            log.info("Keycloak Admin Client initialized successfully");
        }
        return keycloak;
    }
}