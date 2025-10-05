package com.company.sharefile.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.Map;

@Path("/realms/sharefile/protocol/openid-connect")
@RegisterRestClient(configKey = "keycloak")
public interface KeycloakTokenClient {

    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Map<String, Object> getToken(MultivaluedMap<String, String> formData);

    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    void logout(MultivaluedMap<String, String> formData);
}