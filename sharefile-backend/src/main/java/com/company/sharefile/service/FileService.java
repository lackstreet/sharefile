package com.company.sharefile.service;

import com.company.sharefile.entity.FileEntity;
import com.company.sharefile.entity.UserEntity;
import com.company.sharefile.exception.ApiException;
import com.company.sharefile.utils.UploadStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class FileService {
    @Inject
    Logger log;

    @Inject
    AzureStorageService azureStorageService;

    @Inject
    UserService userService;

    @Inject
    EncryptionService encryptionService;

    private String calcolateChecksum(byte[] fileBytes){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(fileBytes);

            StringBuilder hexString = new StringBuilder();
            for(byte b : hashBytes){
                String hex =Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }catch (Exception e){
            throw new ApiException(
                    String.format("Error calculating checksum: %s", e.getMessage()),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-003"
            );
        }
    }

    @Transactional
    public FileEntity uploadFile(InputStream fileData, String fileName, Long fileSize, String mimeType, String keycloakId){
        try{
            log.infof("uploading file %s for keycloakId %s", fileName, keycloakId);

            UserEntity user = userService.findByKeycloakId(keycloakId);
            if(user == null){
                throw new ApiException(
                        String.format("user with keycloakId %s not found", keycloakId),
                        Response.Status.NOT_FOUND,
                        "LAM-404-003"
                );
            }

            if(!userService.hasAvailableQuota(keycloakId, fileSize)){
                throw new ApiException(
                        String.format("Insufficient storage quota. Required: %d bytes", fileSize),
                        Response.Status.BAD_REQUEST,
                        "FILE-400-001"
                );
            }

            log.debugf("calculating checksum for file %s", fileName);
            byte[] fileBytes = fileData.readAllBytes();
            String checksum = calcolateChecksum(fileBytes);
            log.debugf("checksum %s", checksum);

            FileEntity existingFile = FileEntity.find("checksum = ?1 and isDeleted = false", checksum)
                    .firstResult();

            if(existingFile != null){
                log.infof("file %s already exists. creating duplicate reference", fileName);
                return virtualDuplication(existingFile, fileName, user);
            }

            log.debugf("create encryption key for file %s", fileName);
            String encryptionKey = encryptionService.generateKey();

            log.debugf("encrypting file %s", fileName);
            InputStream fileStream =new ByteArrayInputStream(fileBytes);
            InputStream encryptedStream = encryptionService.encrypt(fileStream, encryptionKey);

            log.debugf("uploading encrypted file %s to Azure Storage", fileName);
            Map<String, String> metadata = new HashMap<>();
            metadata.put("originalFileName", fileName);
            metadata.put("checksum", checksum);
            metadata.put("uploadedBy", keycloakId);

            String storagePath = generateStoragePath(user.getId(), fileName);

            byte[] encryptedBytes = encryptedStream.readAllBytes();
            InputStream uploadStream = new ByteArrayInputStream(encryptedBytes);
            azureStorageService.upload(storagePath,uploadStream,(long) encryptedBytes.length,metadata);

            log.debugf("creating file record in database for file %s", fileName);
            FileEntity newFile = FileEntity.builder()
                    .fileName(fileName)
                    .fileSize(fileSize)
                    .mimeType(mimeType)
                    .checksum(checksum)
                    .storagePath(storagePath)
                    .encryptionKey(encryptionKey)
                    .isEncrypted(true)
                    .encryptionAlgorithm("AES-GCM-256")
                    .uploadStatus(UploadStatus.COMPLETED)
                    .uploadedAt(LocalDateTime.now())
                    .createdBy(user)
                    .isVirusScanned(false)
                    .isDeleted(false)
                    .build();

            newFile.persist();

            log.debug("incrementing user storage used");
            userService.incrementStorageUsed(keycloakId, fileSize);
            log.infof("file %s uploaded successfully", fileName);
            return newFile;


        }catch (Exception e){
            throw new ApiException(
                    String.format("Errore durante l'upload del file %s", fileName),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-002"
            );
        }

    }

    private FileEntity virtualDuplication(FileEntity existingFile, String fileName, UserEntity user) {
        log.infof("create duplicated record for the same file", existingFile.getStoragePath());

        FileEntity newFile = FileEntity.builder()
                .fileName(fileName)
                .fileSize(existingFile.getFileSize())
                .mimeType(existingFile.getMimeType())
                .checksum(existingFile.getChecksum())
                .storagePath(existingFile.getStoragePath())
                .encryptionKey(existingFile.getEncryptionKey())
                .isEncrypted(true)
                .encryptionAlgorithm("AES-256-GCM")
                .uploadStatus(UploadStatus.COMPLETED)
                .uploadedAt(LocalDateTime.now())
                .createdBy(user)
                .isVirusScanned(existingFile.getIsVirusScanned())
                .isDeleted(false)
                .build();

        newFile.persist();

        userService.incrementStorageUsed(user.getKeycloakId(), existingFile.getFileSize());

        log.infof("duplicated file %s)",
                 existingFile.getId());

        return newFile;
    }

    private String generateStoragePath(UUID userId, String fileName) {
        String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return String.format("files/%s/%s.enc", userId, sanitizedFileName);
    }

    @Transactional
    public FileEntity getFileById(@NotBlank(message = "fileId is required") String fileId,@NotBlank String keycloakId) {
        return FileEntity.find("id = ?1 and createdBy.keycloakId = ?2 and isDeleted = false", UUID.fromString(fileId), keycloakId)
                .firstResult();
    }

    @Transactional
    public byte[] downloadFileAsBytes(@NotBlank(message = "fileId is required") String fileId,@NotBlank String keycloakId) {

        try{
            FileEntity fileEntity = getFileById(fileId, keycloakId);
            if(fileEntity == null){
                throw new ApiException(
                        String.format("File with ID %s not found", fileId),
                        Response.Status.NOT_FOUND,
                        "LAM-404-001"
                );
            }
            log.infof("Downloading file %s from azure", fileEntity.getFileName());
            byte[] encryptedData = azureStorageService.downloadAsBytes(fileEntity.getStoragePath());

            log.infof("Decrypting file %s", fileEntity.getFileName());
            byte[] decryptedData = encryptionService.decrypt(encryptedData, fileEntity.getEncryptionKey());

            log.infof("Decrypted to %d bytes", decryptedData.length);
            return decryptedData;
        }catch (Exception e){
            throw new ApiException(
                    String.format("Error downloading file %s: %s", fileId, e.getMessage()),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-004"
            );
        }
    }
}
