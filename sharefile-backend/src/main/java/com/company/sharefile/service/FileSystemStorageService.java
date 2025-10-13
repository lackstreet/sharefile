package com.company.sharefile.service;

import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

@ApplicationScoped
@DefaultBean
public class FileSystemStorageService implements StorageService {

    private static final Logger LOG = Logger.getLogger(FileSystemStorageService.class);

    @ConfigProperty(name = "upload.directory")
    String storagePath;

    @Override
    public StorageResult storeFile(InputStream inputStream,
                                   String originalFilename,
                                   String contentType,
                                   long fileSize) throws IOException {
        try {
            String storedFileName = generateStoredFileName(originalFilename);

            Path storageDirPath = Paths.get(storagePath);
            Files.createDirectories(storageDirPath);

            Path finalFilePath = storageDirPath.resolve(storedFileName);
            long actualSize = Files.copy(inputStream, finalFilePath, StandardCopyOption.REPLACE_EXISTING);

            String checksum = calculateSHA256(finalFilePath);

            LOG.infof("✅ File saved: %s (size: %d)", storedFileName, actualSize);

            return new StorageResult(finalFilePath.toString(), checksum, actualSize);

        } catch (Exception e) {
            LOG.errorf(e, "❌ Failed to save file: %s", originalFilename);
            throw new IOException("Filesystem storage failed", e);
        }
    }

    @Override
    public StorageResult storeFileFromTemp(File tempFile,
                                           String originalFilename,
                                           String contentType) throws IOException {
        try {
            String storedFileName = generateStoredFileName(originalFilename);

            Path storageDirPath = Paths.get(storagePath);
            Files.createDirectories(storageDirPath);

            Path finalFilePath = storageDirPath.resolve(storedFileName);
            Files.move(tempFile.toPath(), finalFilePath, StandardCopyOption.REPLACE_EXISTING);
            long actualSize = Files.size(finalFilePath);

            String checksum = calculateSHA256(finalFilePath);

            LOG.infof("✅ File moved: %s (size: %d)", storedFileName, actualSize);

            return new StorageResult(finalFilePath.toString(), checksum, actualSize);

        } catch (Exception e) {
            LOG.errorf(e, "❌ Failed to move file: %s", originalFilename);
            throw new IOException("Filesystem storage failed", e);
        }
    }

    @Override
    public InputStream retrieveFile(String storagePath) throws IOException {
        return new FileInputStream(storagePath);
    }

    @Override
    public void deleteFile(String storagePath) throws IOException {
        Files.deleteIfExists(Paths.get(storagePath));
        LOG.infof("🗑️ Deleted: %s", storagePath);
    }

    @Override
    public boolean fileExists(String storagePath) {
        return Files.exists(Paths.get(storagePath));
    }

    @Override
    public FileInfo getFileInfo(String storagePath) throws IOException {
        Path path = Paths.get(storagePath);
        long size = Files.size(path);
        String contentType = Files.probeContentType(path);
        return new FileInfo(storagePath, size, contentType);
    }

    private String generateStoredFileName(String originalFilename) {
        String fileExtension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + fileExtension;
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot) : "";
    }

    private String calculateSHA256(Path filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(filePath);
        byte[] hashBytes = digest.digest(fileBytes);
        return HexFormat.of().formatHex(hashBytes);
    }
}