package com.company.sharefile.service;

import com.company.sharefile.entity.TransferEntity;
import com.company.sharefile.entity.TransferRecipientEntity;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class NotificationService {

    @Inject
    Logger log;

    @ConfigProperty(name = "sharefile.base.url", defaultValue = "http://localhost:8081")
    String baseUrl;

    @Inject
    Mailer mailer;


    @Inject
    @Location("emails/transfer-notification")
    Template transferNotificationTemplate;

    public void sendTransferNotificationToRecipient(TransferEntity transfer, TransferRecipientEntity recipient) {
        log.infof("Sending transfer notification for transfer %s to recipient %s", transfer.getId(),recipient.getRecipientEmail());
        try {
                sendTransferNotificationEmail(transfer, recipient);
                recipient.setNotifiedAt(LocalDateTime.now());
                recipient.persist();

        } catch (Exception e) {
            log.errorf("Error sending transfer notification for transfer %s: %s", transfer.getId(), e.getMessage());
        }
    }

    private void sendTransferNotificationEmail(
            TransferEntity transfer,
            TransferRecipientEntity recipient) {

        String recipientEmail = recipient.getRecipientEmail();
        String senderName = transfer.getCreatedBy().getFirstName();
        String senderEmail = transfer.getCreatedBy().getEmail();

        String subject = senderName + " ha condiviso dei file con te: " + transfer.getTitle();

        String downloadUrl = buildDownloadUrl(
                transfer.getShareLink(),
                recipient.getAccessToken(),
                recipientEmail
        );

        String totalSize = formatFileSize(transfer.getTotalSizeBytes());
        String expiresAt = formatDate(transfer.getExpiresAt());
        long expiresInDays = ChronoUnit.DAYS.between(LocalDateTime.now(), transfer.getExpiresAt());

        List<Map<String, String>> filesInfo = transfer.getFiles().stream()
                .map(f -> Map.of(
                        "name", f.getFileName(),
                        "mimeType", f.getMimeType().split("/")[1]
                ))
                .toList();

        String htmlBody = transferNotificationTemplate
                .data("title", transfer.getTitle())
                .data("senderName", senderName)
                .data("senderEmail", senderEmail)
                .data("message", transfer.getMessage())
                .data("files", filesInfo)
                .data("fileCount", filesInfo.size())
                .data("totalSize", totalSize)
                .data("expiresAt", expiresAt)
                .data("expiresInDays", expiresInDays)
                .data("downloadUrl", downloadUrl)
                .data("baseUrl", baseUrl)
                .render();

        mailer.send(Mail.withHtml(recipientEmail, subject, htmlBody));
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    private String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dateTime.format(formatter);
    }

    private String buildDownloadUrl(String shareLink, String accessToken, String recipientEmail) {
        return String.format("%s/api/v1/transfers/%s/download/%s?email=%s",
                baseUrl,
                shareLink,
                accessToken,
                recipientEmail
        );
    }

}
