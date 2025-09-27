package com.company.sharefile.entity;

import io.quarkus.arc.runtime.ArcRecorder;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_keycloak_id", columnList = "keycloak_id"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = {"uploadedFiles", "sharedLinks"})
public class User extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "keycloak_id", unique = true, nullable = false, updatable = false)
    @NotBlank(message = "Keycloak ID cannot be blank")
    @Size(max = 255, message = "Keycloak ID cannot be longer than 255 characters")
    private String keycloakId;

    @Column(name = "username", unique = true, nullable = false)
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 255, message = "Username must be between 3 and 255 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "Username can only contain alphanumeric characters, underscores and dashes")
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    @NotBlank(message = "Email cannot be blank")
    @Size(min = 3, max = 255, message = "Email must be between 3 and 255 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", message = "Email is not valid")
    private String email;

    @Column(name = "first_name")
    @Size(max = 255, message = "First name must be less than 255 characters")
    private String firstName;

    @Column(name = "last_name")
    @Size(max = 255, message = "Last name must be less than 255 characters")
    private String lastName;

    @Size(max = 255, message = "Company must be less than 255 characters")
    private String company;

    @Size(max = 255, message = "Department must be less than 255 characters")
    private String department;

    @Column(name = "is_active", nullable = false)
    @NotNull
    private Boolean isActive = true;

    @Column(name = "storage_quota_bytes", nullable = false)
    @NotNull
    @Min(value = 0, message = "Storage quota cannot be negative")
    private Long storageQuotaBytes = 5368709120L; // 5GB default

    @Column(name = "used_storage_bytes", nullable = false)
    @NotNull
    @Min(value = 0, message = "Used storage cannot be negative")
    private Long usedStorageBytes = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    //@OneToMany(mappedBy = "uploadedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    //private List<File> uploadedFiles = new ArrayList<>();

    //@OneToMany(mappedBy = "sharedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    //private List<SharedLink> sharedLinks = new ArrayList<>();

    // Lifecycle callbacks (solo per timestamp)
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

    public static User findByKeycloakId(String keycloakId) {
        return find("keycloakId", keycloakId).firstResult();
    }

    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public static User findByUsername(String username) {
        return find("username", username).firstResult();
    }


}
