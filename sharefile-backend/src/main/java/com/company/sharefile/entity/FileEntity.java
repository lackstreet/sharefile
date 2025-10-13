package com.company.sharefile.entity;

import com.company.sharefile.utils.UploadStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity
@Table(name = "files", indexes = {
        @Index(name = "idx_file_created_by", columnList = "created_by"),
        @Index(name = "idx_file_created_at", columnList = "created_at"),
        @Index(name = "idx_file_deleted", columnList = "is_deleted"),
        @Index(name = "idx_file_checksum", columnList = "checksum_sha256"),
        @Index(name = "idx_file_status", columnList = "upload_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"createdBy"})
public class FileEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "file_name", nullable = false, length = 255)
    @NotBlank(message = "File name cannot be blank")
    @Size(max = 255, message = "File name must be less than 255 characters")
    private String fileName;

    @Column(name = "file_size", nullable = false)
    @NotNull(message = "File size cannot be null")
    @Min(value = 0, message = "File size cannot be negative")
    private Long fileSize = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull(message = "File creation date cannot be null")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull(message = "Created by user cannot be null")
    private UserEntity createdBy;

    @Column(name = "mime_type", length = 128)
    @Size(max = 128, message = "Mime type must be less than 128 characters")
    private String mimeType;

    @Column(name = "checksum_sha256", length = 64, unique = true, nullable = false)
    @NotBlank(message = "Checksum cannot be blank")
    @Size(max = 64, message = "SHA-256 checksum must be exactly 64 characters")
    @Pattern(regexp = "^[a-fA-F0-9]{64}$", message = "Checksum must be a valid SHA-256 hex string")
    @EqualsAndHashCode.Include
    private String checksum;


    @Enumerated(EnumType.STRING)
    @Column(name = "upload_status", nullable = false, length = 32)
    @NotNull(message = "Upload status cannot be null")
    private UploadStatus uploadStatus = UploadStatus.PENDING;

    @Column(name = "storage_path", length = 512)
    @Size(max = 512, message = "Storage path must be less than 512 characters")
    private String storagePath;


    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;


    @Column(name = "encryption_iv", length = 32)
    @Size(max = 32, message = "IV must be less than 32 characters")
    private String encryptionIv;

    @Column(name = "is_encrypted", nullable = false)
    @NotNull(message = "Encryption status cannot be null")
    private Boolean isEncrypted = false;

    @Column(name = "encryption_algorithm", length = 64)
    @Size(max = 64, message = "Encryption algorithm must be less than 64 characters")
    private String encryptionAlgorithm;


    @Column(name = "is_virus_scanned", nullable = false)
    @NotNull(message = "Virus scan status cannot be null")
    private Boolean isVirusScanned = false;

    @Column(name = "virus_scan_result", length = 512)
    private String virusScanResult;


    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "is_deleted", nullable = false)
    @NotNull(message = "Deleted status cannot be null")
    private Boolean isDeleted = false;

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        // Se encryption abilitata, imposta algoritmo default
        if (Boolean.TRUE.equals(isEncrypted) && encryptionAlgorithm == null) {
            encryptionAlgorithm = "AES-256-GCM";
        }
    }
}