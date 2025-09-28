package com.company.sharefile.resource.api.test;

import com.company.sharefile.entity.FileEntity;
import com.company.sharefile.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;


//@Path("/api/test")
//@Produces(MediaType.APPLICATION_JSON)
//@Consumes(MediaType.APPLICATION_JSON)
public class TestResource {
//
//    @Inject
//    EntityManager em;
//
//    @GET
//    @Path("/user")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Transactional
//    public String testUser() {
//        // Test creazione user
//        UserEntity user = new UserEntity();
//        user.setKeycloakId("test-keycloak-123");
//        user.setUsername("testuser");
//        user.setEmail("test@sharefile.com");
//        user.persist();
//
//        // Test query
//        //UserEntity found = UserEntity.findByEmail("test@sharefile.com");
//
//        return "User created with ID: " + found.getId() +
//                ", Username: " + found.getUsername();
//    }
//
//    @GET
//    @Path("/count")
//    @Produces(MediaType.APPLICATION_JSON)
//    public String countUsers() {
//        //long count = UserEntity.count();
//        //return "Total users: " + count;
//    }
//    @POST
//    @Path("/create")
//    @Transactional
//    @Operation(
//            summary = "Create test file",
//            description = "Creates a test file with auto-generated storedFilename for testing purposes"
//    )
//    @APIResponse(
//            responseCode = "200",
//            description = "File created successfully",
//            content = @Content(mediaType = "text/plain")
//    )
//    @APIResponse(responseCode = "500", description = "Internal server error")
//    public Response createTestFile() {
//        try {
//            // Prima creiamo un utente di test (se non esiste)
//            UserEntity testUser = UserEntity.find("email", "test@sharefile.com").firstResult();
//            if (testUser == null) {
//                testUser = new UserEntity();
//                testUser.setEmail("test@company.com");
//                testUser.setFirstName("Test2");
//                testUser.setLastName("User2");
//                testUser.setKeycloakId("test-keycloak-id-1232");
//                testUser.setUsername("testuser2");
//                testUser.persist();
//            }
//
//            // Creiamo il File
//            FileEntity file = new FileEntity();
//            file.setOriginalFileName("test-document.pdf");
//            file.setFilePath("/storage/files/");
//            file.setMimeType("application/pdf");
//            file.setFileSizeBytes(1024L);
//            file.setChecksumSha256("abc123def456ghi789jkl012mno345pqr678stu901vwx234yz567890abcdef");
//            file.setUploadedBy(testUser);
//            file.setUploadIp("192.168.1.100");
//
//            // Il storedFilename sarà generato automaticamente nel @PrePersist
//            file.persist();
//
//            return Response.ok()
//                    .entity("File created successfully! ID: " + file.getId() +
//                            ", StoredFilename: " + file.getStoredFileName())
//                    .build();
//
//        } catch (Exception e) {
//            return Response.status(500)
//                    .entity("Error creating file: " + e.getMessage())
//                    .build();
//        }
//    }
//
//    @GET
//    @Path("/list")
//    @Operation(
//            summary = "List all files",
//            description = "Returns a list of all files in the database"
//    )
//    @APIResponse(
//            responseCode = "200",
//            description = "List of files",
//            content = @Content(
//                    mediaType = "application/json",
//                    schema = @Schema(implementation = FileEntity.class)
//            )
//    )
//    @JsonBackReference
//    @JsonIgnoreProperties({"uploadedBy"})
//    public Response listFiles() {
//        try {
//            List<FileEntity> files = FileEntity.listAll();
//            return Response.ok(files).build();
//        } catch (Exception e) {
//            return Response.status(500)
//                    .entity("Error listing files: " + e.getMessage())
//                    .build();
//        }
//    }
//
//    @GET
//    @Path("/find-by-stored/{storedFileName}")
//    @Operation(
//            summary = "Find file by stored filename",
//            description = "Finds a file using its auto-generated stored filename"
//    )
//    @APIResponse(
//            responseCode = "200",
//            description = "File found",
//            content = @Content(schema = @Schema(implementation = FileEntity.class))
//    )
//    @APIResponse(responseCode = "404", description = "File not found")
//    public Response findByStoredFileName(@PathParam("storedFileName") String storedFileName) {
//        try {
//            FileEntity file = FileEntity.findByStoredFileName(storedFileName);
//            if (file != null) {
//                return Response.ok(file).build();
//            } else {
//                return Response.status(404)
//                        .entity("File not found with storedFilename: " + storedFileName)
//                        .build();
//            }
//        } catch (Exception e) {
//            return Response.status(500)
//                    .entity("Error finding file: " + e.getMessage())
//                    .build();
//        }
//    }
//
//    @GET
//    @Path("/find-by-checksum/{checksum}")
//    @Operation(
//            summary = "Find file by checksum",
//            description = "Finds a file using its SHA256 checksum"
//    )
//    public Response findByChecksum(@PathParam("checksum") String checksum) {
//        try {
//            FileEntity file = FileEntity.findByChecksum(checksum);
//            if (file != null) {
//                return Response.ok(file).build();
//            } else {
//                return Response.status(404)
//                        .entity("File not found with checksum: " + checksum)
//                        .build();
//            }
//        } catch (Exception e) {
//            return Response.status(500)
//                    .entity("Error finding file: " + e.getMessage())
//                    .build();
//        }
//    }
//
//    @POST
//    @Path("/test-uniqueness")
//    @Transactional
//    @Operation(
//            summary = "Test stored filename uniqueness",
//            description = "Creates multiple files and verifies that stored filenames are unique"
//    )
//    public Response testStoredFilenameUniqueness() {
//        try {
//            UserEntity testUser = UserEntity.find("email", "test@company.com").firstResult();
//            if (testUser == null) {
//                return Response.status(400)
//                        .entity("Test user not found. Create a file first.")
//                        .build();
//            }
//
//            // Creiamo 3 file e verifichiamo che abbiano storedFilename diversi
//            String[] storedFilenames = new String[3];
//
//            for (int i = 0; i < 3; i++) {
//                FileEntity file = new FileEntity();
//                file.setOriginalFileName("test-file-" + i + ".txt");
//                file.setFilePath("/storage/files/");
//                file.setMimeType("text/plain");
//                file.setFileSizeBytes(512L);
//                file.setChecksumSha256("test" + i + "abc123def456ghi789jkl012mno345pqr678stu901vwx234yz567890");
//                file.setUploadedBy(testUser);
//                file.setUploadIp("192.168.1.100");
//
//                file.persist();
//                storedFilenames[i] = file.getStoredFileName();
//
//                // Piccola pausa per garantire timestamp diversi
//                Thread.sleep(1);
//            }
//
//            // Verifichiamo che siano tutti diversi
//            boolean allUnique = storedFilenames[0] != null &&
//                    storedFilenames[1] != null &&
//                    storedFilenames[2] != null &&
//                    !storedFilenames[0].equals(storedFilenames[1]) &&
//                    !storedFilenames[1].equals(storedFilenames[2]) &&
//                    !storedFilenames[0].equals(storedFilenames[2]);
//
//            return Response.ok()
//                    .entity("Uniqueness test " + (allUnique ? "PASSED" : "FAILED") +
//                            ". Generated filenames: " +
//                            String.join(", ", storedFilenames))
//                    .build();
//
//        } catch (Exception e) {
//            return Response.status(500)
//                    .entity("Error testing uniqueness: " + e.getMessage())
//                    .build();
//        }
//    }
//
//    @DELETE
//    @Path("/cleanup")
//    @Transactional
//    @Operation(
//            summary = "Cleanup test data",
//            description = "Removes all test files and users created by the test endpoints"
//    )
//    @APIResponse(responseCode = "200", description = "Cleanup completed successfully")
//    public Response cleanup() {
//        try {
//            long deletedFiles = FileEntity.delete("uploadedBy.email", "test@company.com");
//            long deletedUsers = UserEntity.delete("email", "test@company.com");
//
//            return Response.ok()
//                    .entity("Cleanup completed. Deleted " + deletedFiles + " files and " + deletedUsers + " users.")
//                    .build();
//        } catch (Exception e) {
//            return Response.status(500)
//                    .entity("Error during cleanup: " + e.getMessage())
//                    .build();
//        }
//    }
}