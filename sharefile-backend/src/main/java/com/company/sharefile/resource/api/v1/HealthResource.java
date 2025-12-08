package com.company.sharefile.resource.api.v1;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/api/v1/health")
public class HealthResource {

    @GET
    @PermitAll
    public Response health() {
        return Response.ok(Map.of("status", "Alive :)")).build();
    }
}
