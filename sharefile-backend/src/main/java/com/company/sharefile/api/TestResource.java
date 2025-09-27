package com.company.sharefile.api;

import com.company.sharefile.entity.User;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/test")
public class TestResource {

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public String testUser() {
        // Test creazione user
        User user = new User();
        user.setKeycloakId("test-keycloak-123");
        user.setUsername("testuser");
        user.setEmail("test@sharefile.com");
        user.persist();

        // Test query
        User found = User.findByEmail("test@sharefile.com");

        return "User created with ID: " + found.getId() +
                ", Username: " + found.getUsername();
    }

    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public String countUsers() {
        long count = User.count();
        return "Total users: " + count;
    }
}