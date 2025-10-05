package com.company.sharefile.service;

import com.company.sharefile.entity.FileEntity;
import com.company.sharefile.entity.UserEntity;
import com.company.sharefile.exception.ApiException;
import com.company.sharefile.repository.FileRepository;
import com.company.sharefile.utils.FileUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.resource.spi.ConfigProperty;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@ApplicationScoped
public class FileUploadService {

    @Inject
    Logger log;

    @Inject
    UserService userService;

    @Inject
    FileRepository fileRepository;

    @Inject
    FileUtils fileUtils;

    @Transactional
    public FileEntity uploadFile(
            UserEntity user,
            String originalFileName,
            String mimeType,
            Long fileSize,
            @NotNull InputStream fileStream,
            String uploadIpAddress
    ) {
        log.infof("Starting upload for user %s: file %s, size %d bytes", user.getId(), originalFileName, fileSize);

        try {
            // 1 Controlla quota
            if (!userService.hasAvailableQuota(user, fileSize)) {
                log.warnf("User %s has no available quota for file %s", user.getId(), originalFileName);
                throw new ApiException(
                        String.format("User %s has no available quota for file %s", user.getId(), originalFileName),
                        Response.Status.REQUEST_ENTITY_TOO_LARGE,
                        "LAM-413-001"
                );
            }

            // 2️ Leggi file in memoria per calcolare checksum e salvare
            byte[] fileBytes = fileStream.readAllBytes();
            String checksum = fileUtils.calculateChecksum(new ByteArrayInputStream(fileBytes));
            log.infof("File checksum calculated: %s", checksum);

            // 3️ Verifica deduplicazione
            FileEntity existingFile = fileRepository.findByCheckSum(checksum);

            if (existingFile != null && !existingFile.getIsDeleted()) {
                log.infof("File with same checksum already exists, creating reference: %s", existingFile.getId());
                return fileUtils.createDuplicateReference(
                        user, originalFileName, mimeType, fileSize,
                        checksum, existingFile, uploadIpAddress
                );
            }

            // 4. File nuovo - salva fisicamente
            String storedFilename = fileUtils.generateStoredFilename(originalFileName);
            Path uploadPath = fileUtils.saveFileToDisk(new ByteArrayInputStream(fileBytes), storedFilename);

            // 5. Crea record DB
            FileEntity file = new FileEntity();
            file.setOriginalFileName(originalFileName);
            file.setStoredFileName(storedFilename);
            file.setFilePath(uploadPath.toString());
            file.setMimeType(mimeType);
            file.setFileSizeBytes(fileSize);
            file.setChecksumSha256(checksum);
            file.setUploadedBy(user);
            file.setUploadIp(uploadIpAddress);
            file.setIsVirusScanned(false); // TODO: integra ClamAV
            fileRepository.persist(file);
            if(!file.isPersistent())
                throw new ApiException ("File record could not be created", Response.Status.INTERNAL_SERVER_ERROR, "LAM-500-001");

            // 6. Aggiorna quota utente
            userService.incrementUsedStorage(user, fileSize);

            log.infof("File uploaded successfully: id=%s, path=%s",
                    file.getId(), uploadPath);

            return file;

        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Error uploading file", e);
            throw new ApiException("Internal server error during file upload",
                    Response.Status.INTERNAL_SERVER_ERROR, "LAM-500-001");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
