package com.company.sharefile.service;

import com.company.sharefile.dto.v1.records.request.TransferRequestDTO;
import com.company.sharefile.dto.v1.records.response.FileDataResponseDTO;
import com.company.sharefile.dto.v1.records.response.TransferResponseDTO;
import com.company.sharefile.entity.FileEntity;
import com.company.sharefile.entity.TransferEntity;
import com.company.sharefile.entity.TransferRecipientEntity;
import com.company.sharefile.entity.UserEntity;
import com.company.sharefile.exception.ApiException;
import com.company.sharefile.repository.UserRepository;
import com.company.sharefile.utils.GeneratorUtils;
import com.company.sharefile.utils.TransferStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransferService {
    @Inject
    Logger log;
    @Inject
    UserRepository userRepository;

    @Inject
    FileService fileService;

    @Inject
    NotificationService notificationService;


    private TransferEntity validateTransferRequest(String shareLink, String email, String accessToken) {
        TransferEntity transfer = TransferEntity.find("shareLink", shareLink).firstResult();
        if(transfer == null){
            throw new ApiException(
                    String.format("Transfer not found for shareLink: %s", shareLink),
                    Response.Status.NOT_FOUND,
                    "LAM-404-002"
            );
        }

        TransferRecipientEntity recipient = transfer.getRecipients().stream()
                .filter(r -> r.getRecipientEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);

        if(recipient == null){
            throw new ApiException(
                    String.format("Recipient with email %s not found for transfer %s", email, shareLink),
                    Response.Status.NOT_FOUND,
                    "LAM-404-003"
            );
        }

        if(transfer.isExpired()){
            throw new ApiException(
                    String.format("Transfer with shareLink %s is expired", shareLink),
                    Response.Status.GONE,
                    "LAM-410-001"
            );
        }

        if(!recipient.getAccessToken().equals(accessToken)){
            throw new ApiException(
                    String.format("Invalid access token for recipient %s and transfer %s", email, shareLink),
                    Response.Status.UNAUTHORIZED,
                    "LAM-401-001"
            );
        }

        if(transfer.hasReachedDownloadLimit()){
            throw new ApiException(
                    String.format("Transfer with shareLink %s has reached its download limit", shareLink),
                    Response.Status.FORBIDDEN,
                    "LAM-403-001"
            );
        }

        if(transfer.getFiles() == null || transfer.getFiles().isEmpty()){
            throw new ApiException(
                    String.format("No files found for transfer id: %s", transfer.getId()),
                    Response.Status.NOT_FOUND,
                    "LAM-404-004"
            );
        }

        int transferUpdated = TransferEntity.update(
                "downloadCount = downloadCount + 1 WHERE id = ?1",
                transfer.getId()
        );

        if (transferUpdated == 0) {
            log.errorf("Failed to update transfer download count for id: %s", transfer.getId());
        }

        int recipientUpdated = TransferRecipientEntity.update(
                "downloadCount = downloadCount + 1 WHERE id = ?1",
                recipient.getId()
        );

        if (recipientUpdated == 0) {
            log.errorf("Failed to update recipient download count for id: %s", recipient.getId());
        }


        return transfer;
    }

    @Transactional
    public FileDataResponseDTO download(
            @NotBlank @Size(min = 16, max = 16) String shareLink,
            @Email String email,
            @Size(min = 1, max = 64) @NotBlank String accessToken) {

        log.infof("Downloading transfer with shareLink: %s for email: %s", shareLink, email);

        try {
            TransferEntity transfer = validateTransferRequest(shareLink, email, accessToken);
            List<FileEntity> files = transfer.getFiles();

            if(files.size() == 1) {
                return fileService.downloadSingleFile(files.get(0));
            } else {
                return fileService.downloadAsZip(files, transfer.getTitle().concat(".zip"));
            }

        } catch(ApiException e) {
            throw e;
        } catch(Exception e) {
            throw new ApiException(
                    String.format("Error downloading transfer: %s", e.getMessage()),
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "LAM-500-002"
            );
        }
    }


    private TransferEntity transferInitialize(TransferRequestDTO request, UserEntity userEntity) {

        if (request.files() == null || request.files().isEmpty()) {
            throw new ApiException(
                    "At least one file is required",
                    Response.Status.BAD_REQUEST,
                    "LAM-400-001"
            );
        }
        if (request.recipientEmails() == null || request.recipientEmails().isEmpty()) {
            throw new ApiException(
                    "At least one recipient is required",
                    Response.Status.BAD_REQUEST,
                    "LAM-400-002"
            );
        }

        String shareLink = GeneratorUtils.generateUniqueShareLink();
        log.infof("Generated share link: %s", shareLink);

        int days = (request.expiresInDays() != null && request.expiresInDays() > 0)
                ? request.expiresInDays().intValue()
                : 7;

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(days);
        log.infof("Transfer expires at: %s", expiresAt);

        TransferEntity transfer = TransferEntity.builder()
                .title(request.title())
                .message(request.message())
                .shareLink(shareLink)
                .createdBy(userEntity)
                .createdAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .status(TransferStatus.PENDING)
                .totalSizeBytes(0L)
                .downloadLimit(null)
                .downloadCount(0)
                .isExpired(false)
                .build();

        transfer.persist();
        log.infof("transfer created with id: %s", transfer.getId());
        return transfer;
    }

    private LinkFilesResult linkFileToTransfer(List<String> fileIds, TransferEntity transfer, UserEntity userEntity) {
        long totalSize = 0L;
        List<FileEntity> validFiles = new ArrayList<>();
        for(String fileId : fileIds){
            FileEntity fileEntity = FileEntity.findById(UUID.fromString(fileId));
            if(fileEntity == null){
                log.warnf("File not found for id: %s", fileId);
                continue;
            }
            if(fileEntity.getIsDeleted()){
                log.warnf("File is deleted for id: %s", fileId);
                continue;
            }
            if(fileEntity.getCreatedBy().getId() != userEntity.getId()){
                log.warnf("File does not belong to user for id: %s", fileId);
                continue;
            }
            totalSize += fileEntity.getFileSize();
            validFiles.add(fileEntity);
        }

        if(totalSize > 0L){
            log.infof("Total size of linked files: %d bytes", totalSize);
            transfer.setTotalSizeBytes(totalSize);
            transfer.setFiles(validFiles);
            transfer.persist();
        }
        return new LinkFilesResult(validFiles.stream().map(FileEntity::getFileName).toList(), totalSize);
    }

    private List<String> createRecipients(TransferEntity transfer, List<String> recipientEmails , boolean notifyOnCreation) {
        List<String> addedEmails = new ArrayList<>();
        for(String email : recipientEmails){
            String normalizedEmail = email.trim().toLowerCase();
            boolean exists =transfer.getRecipients().stream().anyMatch(r -> r.getRecipientEmail().equalsIgnoreCase(normalizedEmail));
            if(exists){
                log.warnf("Recipient email already exists: %s", normalizedEmail);
                continue;
            }
            String accessToken = GeneratorUtils.generateSecureToken();

            TransferRecipientEntity recipient = TransferRecipientEntity.builder()
                    .transfer(transfer)
                    .recipientEmail(normalizedEmail)
                    .accessToken(accessToken)
                    .createdAt(LocalDateTime.now())
                    .downloadCount(0)
                    .build();

            recipient.persist();

            if (notifyOnCreation) {
                log.infof("Sending notification emails for transfer id %s", transfer.getId());
                try{
                    notificationService.sendTransferNotificationToRecipient(transfer, recipient);
                }catch(Exception e){
                    log.errorf("Error sending notification emails for transfer id %s: %s", transfer.getId(), e.getMessage());
                }
            }
            addedEmails.add(normalizedEmail);
            log.infof("Recipient created: %s", recipient.getId());
        }
        return addedEmails;

    }
    @Transactional
    public TransferResponseDTO createTransfer(@Valid TransferRequestDTO request, String keycloakId) {
        UserEntity userEntity = userRepository.findByKeycloakId(keycloakId);
        if(userEntity == null){
            throw new ApiException(
                    String.format("User not found for keycloakId: %s", keycloakId),
                    Response.Status.NOT_FOUND,
                    "LAM-404-001"
            );
        }

        log.infof("Received transfer request.title: %s from keycloakId %s", request.title(), keycloakId);
        TransferEntity transfer = transferInitialize(request, userEntity);

        log.infof("linking files to transfer id: %s", transfer.getId());

        LinkFilesResult linkResult = linkFileToTransfer(request.files(), transfer, userEntity);
        if(linkResult.totalSize <= 0 || linkResult.fileNames.isEmpty()){
            throw new ApiException(
                    String.format("No files found for transfer id: %s", transfer.getId()),
                    Response.Status.BAD_REQUEST,
                    "LAM-400-003"
            );
        }

        log.infof("Files linked: %d",linkResult.fileNames.size());

        log.infof("Create receipients for transfer id: %s", transfer.getId());

        List<String> addedRecipients = createRecipients(transfer, request.recipientEmails(),request.notifyRecipient());

        if (addedRecipients.isEmpty()) {
            throw new ApiException(
                    String.format("No recipients found for transfer id: %s", transfer.getId()),
                    Response.Status.BAD_REQUEST,
                    "LAM-400-004"
            );
        }
        log.infof("Recipients created: %d", addedRecipients.size());

        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setCompletedAt(LocalDateTime.now());
        transfer.persist();

        log.infof("Transfer completed: %s", transfer.getId());
        log.infof("Share link: %s", transfer.getShareLink());

        return new TransferResponseDTO(
                transfer.getId(),
                transfer.getTitle(),
                transfer.getMessage(),
                transfer.getShareLink(),
                transfer.getCreatedAt(),
                transfer.getExpiresAt(),
                transfer.getTotalSizeBytes(),
                linkResult.fileNames,
                addedRecipients
        );

    }

    private static class LinkFilesResult {
        final List<String> fileNames;
        final long totalSize;

        LinkFilesResult(List<String> fileNames, long totalSize) {
            this.fileNames = fileNames;
            this.totalSize = totalSize;
        }
    }


}