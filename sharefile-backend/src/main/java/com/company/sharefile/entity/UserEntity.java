package com.company.sharefile.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_keycloak_id", columnList = "keycloak_id"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class UserEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "keycloak_id", unique = true, nullable = false, updatable = false)
    @NotNull(message = "Keycloak ID cannot be blank")
    @EqualsAndHashCode.Include
    private String keycloakId;

    @Column(name = "username", length = 32, unique = true, nullable = false)
    @Size(min = 3, max = 32, message = "Username must be between 3 and 32 characters")
    private String username;

    @Column(name = "email", length = 64, unique = true, nullable = false)
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be a valid email")
    private String email;

    @Column(name = "first_name", length = 64)
    @Size(max = 64, message = "First name must be less than 64 characters")
    private String firstName;

    @Column(name = "last_name", length = 64)
    @Size(max = 64, message = "Last name must be less than 64 characters")
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDateTime dateOfBirth;

    @Column(name = "company", length = 64)
    @Size(max = 64, message = "Company must be less than 64 characters")
    private String company;

    @Column(name = "department", length = 64)
    @Size(max = 64, message = "Department must be less than 64 characters")
    private String department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_plan_id")
    private PlanEntity storagePlan;

    @Column(name = "used_storage_bytes", nullable = false)
    @NotNull(message = "Used storage cannot be null")
    @Min(value = 0, message = "Used storage cannot be negative")
    private Long usedStorageBytes = 0L;

    @Column(name = "email_notifications", nullable = false)
    @NotNull(message = "Email notifications setting cannot be null")
    private Boolean emailNotifications = true;

    @Column(name = "is_active", nullable = false)
    @NotNull(message = "Active status cannot be null")
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "role", length = 16, nullable = false)
    @NotNull(message = "Role cannot be null")
    private String role = "GUEST";

    @Column(name = "avatar_url", length = 255)
    @Size(max = 255, message = "Avatar URL must be less than 255")
    private String avatarUrl;

    @Column(name = "language", length = 8, nullable = false)
    @Size(max = 8, message = "Language must be less than 8 characters")
    @NotNull(message = "Language cannot be null")
    private String language = "en-US";

    @PrePersist
    protected void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
