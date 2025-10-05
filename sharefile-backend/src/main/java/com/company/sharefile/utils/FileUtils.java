package com.company.sharefile.utils;

import com.company.sharefile.entity.FileEntity;
import com.company.sharefile.entity.UserEntity;
import com.company.sharefile.exception.ApiException;
import com.company.sharefile.repository.FileRepository;
import com.company.sharefile.service.UserService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.resource.spi.ConfigProperty;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.UUID;

@ApplicationScoped
public class FileUtils {
    @Inject
    FileRepository fileRepository;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    UserService userService;

    @Inject
    Logger log;

    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "upload.directory", defaultValue = "/opt/uploads")
    String uploadDirectory;

    public String calculateChecksum(InputStream stream) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int bytesRead;

        while ((bytesRead = stream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }

        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public String generateStoredFilename(String originalFilename) {
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        return UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + extension;
    }

    public void deleteFile(UUID fileId, UserEntity user) {
        FileEntity file = fileRepository.findById(fileId);

        if (file == null) {
            throw new ApiException(
                    "File not found",
                    Response.Status.NOT_FOUND,
                    "FILE-404-001"
            );
        }

        if (!file.getUploadedBy().getId().equals(user.getId()) && !securityIdentity.getRoles().contains("admin")) {
            throw new ApiException(
                    "Unauthorized to delete this file",
                    Response.Status.FORBIDDEN,
                    "FILE-403-001"
            );
        }

        file.setIsDeleted(true);
        file.setDeletedAt(java.time.LocalDateTime.now());

        userService.decrementUsedStorage(user.getId(), file.getFileSizeBytes());

        log.infof("File soft-deleted: id=%s, user=%s", fileId, user.getId());
    }

    public Path saveFileToDisk(InputStream fileStream, String storedFilename) throws Exception {
        Path uploadPath = Path.of(uploadDirectory, storedFilename);
        Files.createDirectories(uploadPath.getParent());

        Files.copy(fileStream, uploadPath, StandardCopyOption.REPLACE_EXISTING);

        log.debugf("File saved to disk: %s", uploadPath);
        return uploadPath;
    }

    public FileEntity createDuplicateReference(
            UserEntity user,
            String originalFileName,
            String mimeType,
            Long fileSize,
            String checksum,
            FileEntity existingFile,
            String uploadIpAddress
    ) {
        FileEntity file = new FileEntity();
        file.setUploadedBy(user);
        file.setOriginalFileName(originalFileName);
        file.setStoredFileName(existingFile.getStoredFileName());
        file.setMimeType(mimeType);
        file.setFileSizeBytes(fileSize);
        file.setChecksumSha256(checksum);
        file.setUploadIp(uploadIpAddress);
        file.setFilePath(existingFile.getFilePath());
        file.setIsDeleted(false);
        fileRepository.persist(file);
        if(file.isPersistent())
            return file;
        else
            throw new ApiException ("File record could not be created", Response.Status.INTERNAL_SERVER_ERROR, "LAM-500-001");

    }

}
