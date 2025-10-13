package com.company.sharefile.service;

import com.company.sharefile.dto.v1.records.response.FileUploadResponseDTO;
import com.company.sharefile.entity.FileEntity;
import com.company.sharefile.entity.TransferEntity;
import com.company.sharefile.exception.ApiException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@ApplicationScoped
public class FileUploadService {

    @Inject
    Logger log;

    @Inject
    StorageService storageService; // ← Usa l'interfaccia!

    @Transactional
    public FileUploadResponseDTO uploadFile(UUID fileId, InputStream fileInputStream) throws IOException {

        FileEntity fileEntity = FileEntity.findById(fileId);

        if (fileEntity == null) {
            throw new ApiException(
                    "File entity not found",
                    Response.Status.NOT_FOUND,
                    "LAM-404-002"
            );
        }

        if (!"pending".equals(fileEntity.getStoredFileName())) {
            throw new ApiException(
                    "File already uploaded",
                    Response.Status.CONFLICT,
                    "LAM-409-001"
            );
        }

        try {
            // Usa StorageService (filesystem o MinIO)
            StorageService.StorageResult result = storageService.storeFile(
                    fileInputStream,
                    fileEntity.getOriginalFileName(),
                    fileEntity.getMimeType(),
                    fileEntity.getFileSizeBytes()
            );

            // Aggiorna FileEntity
            fileEntity.setStoredFileName(extractFileName(result.storagePath()));
            fileEntity.setFilePath(result.storagePath());
            fileEntity.setChecksumSha256(result.checksum());
            fileEntity.persist();

            log.infof("✅ File uploaded: %s -> %s", fileId, result.storagePath());

            // Verifica completamento transfer
            checkTransferCompletion(fileEntity.getTransferId());

            return new FileUploadResponseDTO(
                    fileId,
                    fileEntity.getOriginalFileName(),
                    result.fileSize(),
                    result.checksum(),
                    true
            );

        } catch (Exception e) {
            log.errorf(e, "❌ Upload failed: %s", fileId);
            throw new ApiException(
                    "Error uploading file: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-002"
            );
        }
    }

    @Transactional
    public FileUploadResponseDTO uploadFileFromTemp(String fileIdStr, File tempFile) throws IOException {

        UUID fileId = parseFileId(fileIdStr);
        FileEntity fileEntity = FileEntity.findById(fileId);

        if (fileEntity == null) {
            throw new ApiException(
                    "File entity not found",
                    Response.Status.NOT_FOUND,
                    "LAM-404-002"
            );
        }

        if (!"pending".equals(fileEntity.getStoredFileName())) {
            throw new ApiException(
                    "File already uploaded",
                    Response.Status.CONFLICT,
                    "LAM-409-001"
            );
        }

        try {
            // Usa StorageService
            StorageService.StorageResult result = storageService.storeFileFromTemp(
                    tempFile,
                    "fileEntity.getOriginalFileName()",
                    "fileEntity.getMimeType()"
            );

            // Aggiorna FileEntity
            fileEntity.setStoredFileName(extractFileName(result.storagePath()));
            fileEntity.setFilePath(result.storagePath());
            fileEntity.setChecksumSha256(result.checksum());
            fileEntity.persist();

            log.infof("✅ File uploaded from temp: %s", fileId);

            checkTransferCompletion(fileEntity.getTransferId());

            return new FileUploadResponseDTO(
                    fileId,
                    fileEntity.getOriginalFileName(),
                    result.fileSize(),
                    result.checksum(),
                    true
            );

        } catch (Exception e) {
            log.errorf(e, "❌ Upload from temp failed: %s", fileId);
            throw new ApiException(
                    "Error uploading file: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-002"
            );
        }
    }

    @Transactional
    protected void checkTransferCompletion(TransferEntity transfer) {
        long uploadedCount = FileEntity.count(
                "transferId = ?1 and storedFileName != 'pending'",
                transfer
        );

        log.infof("Transfer %s: %d/%d files uploaded",
                transfer.getId(), uploadedCount, transfer.getTotalFiles());

        if (uploadedCount == transfer.getTotalFiles()) {
            transfer.setStatus(TransferEntity.TransferStatus.EXHAUSTED);
            transfer.persist();
            log.infof("✅ Transfer %s completed", transfer.getId());
        }
    }

    private UUID parseFileId(String fileIdStr) {
        try {
            return UUID.fromString(fileIdStr);
        } catch (IllegalArgumentException e) {
            throw new ApiException(
                    "Invalid file ID format",
                    Response.Status.BAD_REQUEST,
                    "LAM-400-001"
            );
        }
    }

    private String extractFileName(String path) {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash >= 0) {
            return path.substring(lastSlash + 1);
        }
        // Filesystem path (Windows/Unix)
        lastSlash = path.lastIndexOf(File.separator);
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
}