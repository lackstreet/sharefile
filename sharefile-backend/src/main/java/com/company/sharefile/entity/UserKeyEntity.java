package com.company.sharefile.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_encryption_keys", indexes = {
        @Index(name = "idx_user_keys_user_id", columnList = "user_id"),
        @Index(name = "idx_user_keys_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"user"}) // Escludi relazione lazy
public class UserKeyEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    @Column(name = "encryption_key", length = 512, nullable = false)
    @NotNull
    @Size(max = 512, message = "Encryption key must be less than 512 characters")
    private String encryptionKey;

    @Column(name = "algorithm", nullable = false)
    @NotNull
    private String algorithm = "AES-256-GCM";

    @Column(name = "is_active", nullable = false)
    @NotNull
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column( name = "revoked_at" )
    private LocalDateTime revokedAt;

    @PrePersist
    protected void prePersit() {
        createdAt = LocalDateTime.now();
    }

    public void revoke() {
        this.isActive = false;
        this.revokedAt = LocalDateTime.now();
    }

    public boolean isValid() {
        return isActive && revokedAt == null;
    }




}
