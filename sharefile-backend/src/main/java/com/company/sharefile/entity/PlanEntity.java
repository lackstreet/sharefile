package com.company.sharefile.entity;

import com.company.sharefile.utils.PlanType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringExclude;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "storage_plans", indexes = {
        @Index(name = "idx_plan_type", columnList = "plan_type"),
        @Index(name = "idx_plan_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class PlanEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue( strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "description", length = 255)
    @Size(max = 255, message = "Description cannot be longer than 255 characters")
    private String description;

    @Column(name = "storage_quota_bytes", nullable = false)
    @NotNull(message = "Storage quota cannot be null")
    @Min(value = 0, message = "Storage quota cannot be negative")
    private Long storageQuotaBytes;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", message = "Price cannot be negative")
    private BigDecimal price;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false, length = 32)
    private PlanType planType = PlanType.FREE;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
