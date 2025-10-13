package com.company.sharefile.service;

import com.company.sharefile.dto.v1.records.UserInfoDTO;
import com.company.sharefile.dto.v1.records.request.InitTransferRequestDTO;
import com.company.sharefile.dto.v1.records.response.InitTransferResponseDTO;
import com.company.sharefile.entity.FileEntity;
import com.company.sharefile.entity.TransferEntity;
import com.company.sharefile.entity.UserEntity;
import com.company.sharefile.exception.ApiException;
import com.company.sharefile.repository.UserRepository;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TransferService {
    @Inject
    Logger log;
    @Inject
    UserService userService;

    @Transactional
    public InitTransferResponseDTO initialization(InitTransferRequestDTO request) {
        UserInfoDTO user = userService.getCurrentUser();
        UserEntity userEntity = UserEntity.findById(user.id());

        if (userEntity == null) {
            log.errorf("User not found: %s", user.id());
            throw new ApiException(
                    "User not found",
                    Response.Status.NOT_FOUND,
                    "LAM-404-001"
            );
        }

        try {
            log.infof("Init transfer for user %s: %d files", user.username(), request.files().size());

            // 1. Crea il TransferEntity
            TransferEntity transfer = new TransferEntity();
            transfer.setMessage(request.message());
            transfer.setStatus(TransferEntity.TransferStatus.PENDING);
            transfer.setTotalSizeBytes(0L);
            transfer.setTotalSizeBytes(
                    request.files().stream()
                            .mapToLong(InitTransferRequestDTO.FileMetaDataDTO::fileSize)
                            .sum()
            );
            transfer.persist();

            // 2. Crea i FileEntity e genera gli URL di upload
            List<InitTransferResponseDTO.FileUploadUrlDTO> uploadUrls = new ArrayList<>();

            for (var fileMetadata : request.files()) {
                FileEntity file = new FileEntity();
                file.setOriginalFileName(fileMetadata.fileName());
                file.setFileSizeBytes(fileMetadata.fileSize());
                file.setUploadedBy(userEntity);
                file.setTransferId(transfer);
                file.setStoredFileName("pending");
                file.setFilePath("pending");
                file.setChecksumSha256("pending");
                file.setIsDeleted(false);
                file.persist();

                // Genera l'URL per l'upload di questo specifico file
                String uploadUrl = String.format("/api/v1/files/upload/%s", file.getId());

                uploadUrls.add(new InitTransferResponseDTO.FileUploadUrlDTO(
                        file.getId(),
                        fileMetadata.fileName(),
                        uploadUrl
                ));
            }

            // 3. Genera download URL per il transfer
            String downloadUrl = String.format("https://sharefile.com/download/%s", transfer.getId());
            transfer.persist(); // Aggiorna con il downloadUrl

            log.infof("Transfer initialized: %s with %d files", transfer.getId(), uploadUrls.size());

            return new InitTransferResponseDTO(transfer.getId(), uploadUrls);

        } catch (Exception e) {
            log.errorf(e, "Error initializing transfer for user %s", user.username());
            throw new ApiException(
                    String.format("Error initializing transfer: %s", e.getMessage()),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-001"
            );
        }
    }
}
