package com.company.sharefile.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface StorageService {

    /**
     * Salva file da InputStream
     */
    StorageResult storeFile(InputStream inputStream,
                            String originalFilename,
                            String contentType,
                            long fileSize) throws IOException;

    /**
     * Salva file da File temporaneo
     */
    StorageResult storeFileFromTemp(File tempFile,
                                    String originalFilename,
                                    String contentType) throws IOException;

    /**
     * Recupera file
     */
    InputStream retrieveFile(String storagePath) throws IOException;

    /**
     * Elimina file
     */
    void deleteFile(String storagePath) throws IOException;

    /**
     * Verifica esistenza
     */
    boolean fileExists(String storagePath);

    /**
     * Info file
     */
    FileInfo getFileInfo(String storagePath) throws IOException;

    // ============================================
    // Records
    // ============================================

    record StorageResult(
            String storagePath,
            String checksum,
            long fileSize
    ) {}

    record FileInfo(
            String path,
            long size,
            String contentType
    ) {}
}
