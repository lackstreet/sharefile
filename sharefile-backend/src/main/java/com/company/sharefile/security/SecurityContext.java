package com.company.sharefile.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Optional;
import java.util.Set;

@RequestScoped
public class SecurityContext {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    // --- Metodi di Stato e Controlli ---

    public boolean isAuthenticated() {
        return !securityIdentity.isAnonymous();
    }

    public boolean hasRole(Role role) {
        return securityIdentity.hasRole(role.getRoleName());
    }

    public Set<String> getRoles() {
        return securityIdentity.getRoles();
    }

    // --- Metodi per Dati dell'Utente (Richiedono Autenticazione) ---

    // Metodo helper interno per ottenere un claim e lanciare un'eccezione se non autenticato
    private <T> T getAuthenticatedClaim(String claimName) {
        validateAuthentication();
        return jwt.getClaim(claimName);
    }

    // Metodo helper per ottenere un claim che potrebbe essere nullo o assente
    private <T> Optional<T> getOptionalClaim(String claimName) {
        if (!isAuthenticated()) {
            return Optional.empty();
        }
        return Optional.ofNullable(jwt.getClaim(claimName));
    }

    public String getCurrentUserId(){
        // Il subject (ID) DEVE esistere se autenticato
        return getAuthenticatedClaim("sub"); // "sub" è il claim standard per il subject/ID
    }

    public Optional<String> getCurrentUserEmail(){
        return getOptionalClaim("email");
    }

    public Optional<String> getFirstName() {
        return getOptionalClaim("given_name");
    }

    public Optional<String> getLastName() {
        return getOptionalClaim("family_name");
    }

    // --- Metodi SPECIFICI - Resi PUBBLICI per uso comune nell'applicazione ---
    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    public boolean isManager() {
        return hasRole(Role.MANAGER);
    }
    public boolean isUser() {
        return hasRole(Role.USER);
    }
    public boolean isGuest() {
        return hasRole(Role.GUEST);
    }


    private void validateAuthentication() {
        if (!isAuthenticated()) {
            throw new SecurityException("User not authenticated. Cannot access claims.");
        }
    }
}
