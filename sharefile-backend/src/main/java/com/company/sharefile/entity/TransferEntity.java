package com.company.sharefile.entity;

import com.company.sharefile.utils.TransferStatus;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transfers", indexes = {
        @Index(name = "idx_transfer_created_by", columnList = "created_by"),
        @Index(name = "idx_transfer_status", columnList = "status"),
        @Index(name = "idx_transfer_expires_at", columnList = "expires_at"),
        @Index(name = "idx_transfer_created_at", columnList = "created_at"),
        @Index(name = "idx_transfer_share_link", columnList = "share_link")  // ✅ NUOVO
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"createdBy", "files", "recipients"})
public class TransferEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull(message = "Created by user cannot be null")
    private UserEntity createdBy;

    @Column(name = "title", length = 255)
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @Column(name = "message", length = 1000)
    @Size(max = 1000, message = "Message must be less than 1000 characters")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @NotNull(message = "Status cannot be null")
    private TransferStatus status = TransferStatus.PENDING;

    @Column(name = "total_size_bytes", nullable = false)
    @NotNull(message = "Total size cannot be null")
    @Min(value = 0, message = "Total size cannot be negative")
    private Long totalSizeBytes = 0L;

    @Column(name = "download_limit")
    @Min(value = 0, message = "Download limit must be at least 0")
    private Integer downloadLimit;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull(message = "Created at cannot be null")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    // ========== NUOVI CAMPI NECESSARI ==========

    /**
     * Link univoco di condivisione (8 caratteri)
     * Es: "a3f9k2m1"
     */
    @Column(name = "share_link", unique = true, nullable = false, length = 16)
    @NotBlank(message = "Share link cannot be blank")
    @Size(min = 16, max = 16, message = "Share link must be exactly 16 characters")
    private String shareLink;

    /**
     * Contatore di quante volte il transfer è stato scaricato
     */
    @Column(name = "download_count", nullable = false)
    @Min(value = 0, message = "Download count cannot be negative")
    @Builder.Default
    private Integer downloadCount = 0;

    /**
     * Flag per indicare se il transfer è scaduto
     * (può essere calcolato dinamicamente, ma utile per query)
     */
    @Column(name = "is_expired", nullable = false)
    @NotNull(message = "Expired status cannot be null")
    @Builder.Default
    private Boolean isExpired = false;

    // ============================================

    /**
     * File inclusi nel transfer (Many-to-Many).
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "transfer_files",
            joinColumns = @JoinColumn(name = "transfer_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id"),
            indexes = {
                    @Index(name = "idx_tf_transfer", columnList = "transfer_id"),
                    @Index(name = "idx_tf_file", columnList = "file_id")
            }
    )
    @Builder.Default
    private List<FileEntity> files = new ArrayList<>();

    @OneToMany(mappedBy = "transfer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TransferRecipientEntity> recipients = new ArrayList<>();

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        // Auto-calcola scadenza a 7 giorni se non specificata
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusDays(7);
        }
    }

    /**
     * Helper method per verificare se il transfer è scaduto
     */
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        boolean expired = LocalDateTime.now().isAfter(expiresAt);
        if (expired && !this.isExpired) {
            this.isExpired = true;
        }
        return expired;
    }

    /**
     * Helper method per verificare se ha raggiunto il limite di download
     */
    public boolean hasReachedDownloadLimit() {
        if (downloadLimit == null) {
            return false; // Nessun limite
        }
        return downloadCount >= downloadLimit;
    }

}