package com.company.sharefile.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.validation.constraints.*;
import lombok.*;


@Entity
@Table(name = "transfer_recipients", indexes = {
        @Index(name = "idx_recipient_transfer", columnList = "transfer_id"),
        @Index(name = "idx_recipient_token", columnList = "access_token"),
        @Index(name = "idx_recipient_email", columnList = "recipient_email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"transfer"})
public class TransferRecipientEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transfer_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull(message = "Transfer cannot be null")
    private TransferEntity transfer;


    @Column(name = "recipient_email", nullable = false, length = 256)
    @NotBlank(message = "Recipient email cannot be blank")
    @Email(message = "Invalid email format")
    private String recipientEmail;


    @Column(name = "access_token", unique = true, nullable = false, length = 128)
    @NotBlank(message = "Access token cannot be blank")
    @EqualsAndHashCode.Include
    private String accessToken;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Column(name = "downloaded_at")
    private LocalDateTime downloadedAt;


    @Column(name = "download_count", nullable = false)
    @NotNull(message = "Download count cannot be null")
    @Min(value = 0, message = "Download count cannot be negative")
    private Integer downloadCount = 0;


    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull(message = "Created at cannot be null")
    private LocalDateTime createdAt;

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        // Genera token univoco se non presente
        if (accessToken == null) {
          //  accessToken = generateSecureToken();
        }
    }
}