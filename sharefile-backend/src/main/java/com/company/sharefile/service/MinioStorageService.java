package com.company.sharefile.service;

import io.minio.*;
import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.UUID;

@ApplicationScoped
@LookupIfProperty(name = "storage.type", stringValue = "minio")
@Startup
public class MinioStorageService implements StorageService {

    private static final Logger LOG = Logger.getLogger(MinioStorageService.class);

    @ConfigProperty(name = "minio.url")
    String minioUrl;

    @ConfigProperty(name = "minio.access-key")
    String accessKey;

    @ConfigProperty(name = "minio.secret-key")
    String secretKey;

    @ConfigProperty(name = "minio.bucket-name")
    String bucketName;

    private MinioClient minioClient;

    @PostConstruct
    void init() {
        try {
            LOG.infof("🔧 Initializing MinIO storage: %s", minioUrl);

            minioClient = MinioClient.builder()
                    .endpoint(minioUrl)
                    .credentials(accessKey, secretKey)
                    .build();

            createBucketIfNotExists();
            LOG.info("✅ MinIO storage initialized");

        } catch (Exception e) {
            LOG.error("❌ Failed to initialize MinIO", e);
            throw new RuntimeException("MinIO initialization failed", e);
        }
    }

    private void createBucketIfNotExists() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        );

        if (!exists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build()
            );
            LOG.infof("✅ Bucket '%s' created", bucketName);
        }
    }

    @Override
    public StorageResult storeFile(InputStream inputStream,
                                   String originalFilename,
                                   String contentType,
                                   long fileSize) throws IOException {
        DigestInputStream digestStream = null;

        try {
            String storagePath = generateStoragePath(originalFilename);
            LOG.infof("📤 Uploading to MinIO: %s (size: %d)", originalFilename, fileSize);

            digestStream = new DigestInputStream(
                    inputStream,
                    MessageDigest.getInstance("SHA-256")
            );

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storagePath)
                            .stream(digestStream, fileSize, -1)
                            .contentType(contentType)
                            .build()
            );

            String checksum = bytesToHex(digestStream.getMessageDigest().digest());
            LOG.infof("✅ Uploaded: %s (checksum: %s)", storagePath, checksum);

            return new StorageResult(storagePath, checksum, fileSize);

        } catch (Exception e) {
            LOG.errorf(e, "❌ Upload failed: %s", originalFilename);
            throw new IOException("MinIO upload failed", e);
        } finally {
            closeQuietly(digestStream);
            closeQuietly(inputStream);
        }
    }

    @Override
    public StorageResult storeFileFromTemp(File tempFile,
                                           String originalFilename,
                                           String contentType) throws IOException {
        try (FileInputStream fis = new FileInputStream(tempFile)) {
            long fileSize = tempFile.length();
            return storeFile(fis, originalFilename, contentType, fileSize);
        }
    }

    @Override
    public InputStream retrieveFile(String storagePath) throws IOException {
        try {
            LOG.infof("📥 Downloading from MinIO: %s", storagePath);

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storagePath)
                            .build()
            );
        } catch (Exception e) {
            LOG.errorf(e, "❌ Download failed: %s", storagePath);
            throw new IOException("MinIO download failed", e);
        }
    }

    @Override
    public void deleteFile(String storagePath) throws IOException {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storagePath)
                            .build()
            );
            LOG.infof("🗑️ Deleted from MinIO: %s", storagePath);
        } catch (Exception e) {
            LOG.errorf(e, "❌ Delete failed: %s", storagePath);
            throw new IOException("MinIO delete failed", e);
        }
    }

    @Override
    public boolean fileExists(String storagePath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storagePath)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public FileInfo getFileInfo(String storagePath) throws IOException {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storagePath)
                            .build()
            );

            return new FileInfo(storagePath, stat.size(), stat.contentType());
        } catch (Exception e) {
            throw new IOException("Failed to get file info", e);
        }
    }

    private String generateStoragePath(String originalFilename) {
        String datePath = java.time.LocalDate.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        String uuid = UUID.randomUUID().toString();
        String sanitized = originalFilename
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_{2,}", "_")
                .toLowerCase();

        if (sanitized.length() > 100) {
            String extension = "";
            int lastDot = sanitized.lastIndexOf('.');
            if (lastDot > 0) {
                extension = sanitized.substring(lastDot);
                sanitized = sanitized.substring(0, Math.min(95, lastDot)) + extension;
            } else {
                sanitized = sanitized.substring(0, 100);
            }
        }

        return String.format("%s/%s_%s", datePath, uuid, sanitized);
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void closeQuietly(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                LOG.warn("Failed to close stream", e);
            }
        }
    }
}