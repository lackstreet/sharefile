package com.company.sharefile.entity;

import com.company.sharefile.config.ValidationConstants;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shared_links", indexes = {
        @Index(name = "idx_shared_link_token", columnList = "link_token"),
        @Index(name = "idx_shared_link_created_by", columnList = "created_by"),
        @Index(name = "idx_shared_link_expires_at", columnList = "expires_at"),
        @Index(name = "idx_shared_link_active", columnList = "is_active")

})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"createdBy", "files"})
public class SharedLinkEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "link_token", unique = true, nullable = false)
    @NotBlank(message = "Link token cannot be blank")
    @Size(max = ValidationConstants.LINK_TOKEN_LENGTH, message = ValidationConstants.LINK_TOKEN_MESSAGE)
    private String linkToken;

    @Column(name = "title")
    @Size(max = ValidationConstants.MAX_LINK_TITLE_LENGTH, message = "Title cannot be longer than {max} characters")
    private String title;

    @Column(name = "description")
    @Size(max = ValidationConstants.MAX_LINK_DESCRIPTION_LENGTH, message = "Description cannot be longer than {max} characters")
    private String description;

    @Column(name = "recipient_email", nullable = false)
    @Email(message = "Recipient email must be valid")
    @Size(max = ValidationConstants.MAX_EMAIL_LENGTH, message = "Recipient email cannot be longer than {max} characters")
    private String recipientEmail;

    @Column(name = "recipient_name")
    @Size(max = ValidationConstants.MAX_FULL_NAME_LENGTH, message = "Recipient name cannot be longer than {max} characters")
    private String recipientName;

    @Column(name = "sender_message")
    @Size(max = ValidationConstants.MAX_SENDER_MESSAGE_LENGTH, message = "Sender message cannot be longer than {max} characters")
    private String senderMessage;

    @Column(name = "password_hash")
    @Size(max = ValidationConstants.MAX_PASSWORD_HASH_LENGTH, message = "Password hash cannot be longer than {max} characters")
    private String passwordHash;

    @Column(name = "max_downloads")
    @Min(value = 0, message = "Max downloads cannot be negative")
    private Integer maxDownloads;

    @Column(name = "download_count", nullable = false)
    @NotNull
    @Min(value = 0, message = "Download count cannot be negative")
    private Integer downloadCount = 0;

    @Column(name = "is_active", nullable = false)
    @NotNull
    private Boolean isActive = true;

    @Column(name = "is_public", nullable = false)
    @NotNull
    private Boolean isPublic = false;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "last_accessed_ip")
    @Size(max = ValidationConstants.MAX_IP_ADDRESS_LENGTH, message = "Last accessed IP cannot be longer than {max} characters")
    private String lastAccessedIp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @NotNull
    private UserEntity createdBy;

    @ManyToMany
    @JoinTable(
            name = "shared_link_files",
            joinColumns = @JoinColumn (name = "shared_link_id"),
            inverseJoinColumns = @JoinColumn (name = "file_id")
    )
    private List<FileEntity> files = new ArrayList<>();

    private String generateLinkToken() {
        String timePart = Long.toHexString(System.currentTimeMillis()); // timestamp
        String uuidPart = UUID.randomUUID().toString().replace("-", ""); // random
        return (timePart + uuidPart).substring(0, ValidationConstants.LINK_TOKEN_LENGTH); // token finale
    }

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if(this.linkToken == null || this.linkToken.isBlank()) {
            this.linkToken = generateLinkToken();
        }

    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static SharedLinkEntity findByLinkToken(String linkToken) {
        return find("linkToken", linkToken).firstResult();
    }

    public static SharedLinkEntity findActiveByLinkToken(String linkToken) {
        return find("linkToken = ?1 and isActive = true", linkToken).firstResult();
    }

    public static List<SharedLinkEntity> findByCreatedBy(UserEntity user) {
        return find("createdBy", user).list();
    }

    public static List<SharedLinkEntity> findActiveByCreatedBy(UserEntity user) {
        return find("createdBy = ?1 and isActive = true", user).list();
    }

    public static List<SharedLinkEntity> findExpiredLinks() {
        return find("expiresAt < ?1 and isActive = true", LocalDateTime.now()).list();
    }

}
