package com.company.sharefile.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity
@Table(name = "files", indexes = {
        @Index(name = "idx_file_uploaded_by", columnList = "uploaded_by"),
        @Index(name = "idx_file_created_at", columnList = "created_at"),
        @Index(name = "idx_file_deleted", columnList = "is_deleted"),
        @Index(name = "idx_file_checksum", columnList = "checksum_sha256")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"uploadedBy"})
public class FileEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;


    @Column(name = "original_file_name", nullable = false)
    @NotBlank(message = "Original file name cannot be blank")
    @Size(max = 255, message = "Original file name cannot be longer than 255 characters")
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false)
    @NotBlank(message = "Stored file name cannot be blank")
    @Size(max = 512, message = "Stored file name cannot be longer than 255 characters")
    private String storedFileName;

    @Column(name = "file_path", nullable = false)
    @NotBlank(message = "File path cannot be blank")
    @Size(max = 1024, message = "File path cannot be longer than 1024 characters")
    private String filePath;

    @Column(name = "mime_type")
    @Size(max = 255, message = "Mime type cannot be longer than 255 characters")
    private String mimeType;

    @Column(name = "file_size_bytes")
    @NotNull
    @Min(value = 1, message = "File size cannot be less than 0 bytes")
    private Long fileSizeBytes;

    @Column(name = "checksum_sha256", unique = true, nullable = false)
    @Size(max = 64, message = "Checksum cannot be longer than 255 characters")
    private String checksumSha256;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    @NotNull
    private UserEntity uploadedBy;

    @Column(name = "upload_ip")
    @Size(max = 45, message = "Upload IP cannot be longer than 45 characters")
    private String uploadIp;


    @Column(name = "is_virus_scanned", nullable = false)
    @NotNull
    private Boolean isVirusScanned = false;

    @Column(name = "virus_scan_result")
    @Size(max = 50 , message = "Virus scan result cannot be longer than 50 characters")
    private String virusScanResult;

    @Column(name = "is_deleted", nullable = false)
    @NotNull
    private Boolean isDeleted = false;


    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private String generateUniqueStoredFileName() {
        return UUID.randomUUID().toString() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }


    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;

        if (this.storedFileName == null || this.storedFileName.isBlank()) {
            this.storedFileName = generateUniqueStoredFileName();
        }
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }



}
