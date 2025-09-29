package com.company.sharefile.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import org.keycloak.admin.client.Keycloak;

@Readiness
@ApplicationScoped
public class KeycloakHealthCheck implements HealthCheck {

    @Inject
    Keycloak keycloak;

    @Override
    public HealthCheckResponse call() {
        try {
            keycloak.serverInfo().getInfo();
            return HealthCheckResponse.up("Keycloak");
        } catch (Exception e) {
            return HealthCheckResponse.down("Keycloak");
        }
    }
}