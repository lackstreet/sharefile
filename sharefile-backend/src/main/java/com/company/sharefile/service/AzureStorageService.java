package com.company.sharefile.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.company.sharefile.exception.ApiException;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

@ApplicationScoped
public class AzureStorageService {

    @Inject
    Logger log;

    @ConfigProperty(name = "azure.storage.connection-string")
    String connectionString;

    @ConfigProperty(name = "azure.storage.container-name")
    String containerName;

    private BlobServiceClient blobServiceClient;
    private BlobContainerClient containerClient;

    /**
     * Inizializza Azure Blob Service Client al startup
     */
    void onStart(@Observes StartupEvent ev) {
        log.info("☁️ Inizializzazione Azure Blob Storage...");

        try {
            // Crea BlobServiceClient
            blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            // Ottieni o crea container
            containerClient = blobServiceClient.getBlobContainerClient(containerName);

            if (!containerClient.exists()) {
                log.infof("Creazione container: %s", containerName);
                containerClient.create();
                log.infof("Container creato: %s", containerName);
            } else {
                log.infof("Container esistente: %s", containerName);
            }

        } catch (Exception e) {
            log.errorf(e, "Errore inizializzazione Azure Blob Storage");
            throw new RuntimeException("Failed to initialize Azure Blob Storage", e);
        }
    }

    public void upload(String path, InputStream inputStream, long fileEncryptedSize, Map<String, String> metadata ) {
        try{
            log.infof("Uploading file: %s", path);
            BlobClient blobClient = containerClient.getBlobClient(path);
            blobClient.upload(inputStream, fileEncryptedSize,true );
            if(metadata != null && !metadata.isEmpty()){
                blobClient.setMetadata(metadata);
            }
            log.infof("File uploaded successfully: %s", path);

        }catch(Exception e){
            throw new ApiException(
                    String.format("Error uploading file to Azure Blob Storage: %s", path),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-004"
            );
        }
    }

    public byte[] downloadAsBytes(@Size(max = 512, message = "Storage path must be less than 512 characters") String storagePath) {

        try{
            log.infof("Downloading file: %s", storagePath);

            BlobClient blobClient = containerClient.getBlobClient(storagePath);
            if(!blobClient.exists()){
                throw new ApiException(
                        String.format("File not found in Azure Blob Storage: %s", storagePath),
                        Response.Status.NOT_FOUND,
                        "LAM-404-001"
                );
            }
            // Download in ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.downloadStream(outputStream);

            byte[] data = outputStream.toByteArray();
            log.infof("Downloaded %d bytes from blob: %s", data.length, storagePath);
            return data;
        }catch (Exception e){
            throw new ApiException(
                    String.format("Error downloading file from Azure Blob Storage: %s", storagePath),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-005"
            );
        }
    }
}