package org.example.fasthost.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.fasthost.entity.abs.BaseEntity;
import org.example.fasthost.entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_user", columnList = "user_id"),
                @Index(name = "idx_orders_status", columnList = "status"),
                @Index(name = "idx_orders_end_time", columnList = "end_time")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Orders extends BaseEntity {

    /* =========================
       RELATIONS
       ========================= */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariffs tariff;

    /* =========================
       BILLING
       ========================= */

    /** Hosting muddati (KUN bilan) */
    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    /** Buyurtma paytidagi yakuniy narx (snapshot) */
    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    /* =========================
       STATUS
       ========================= */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    /* =========================
       TIME
       ========================= */

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /* =========================
       HOSTING CREDENTIALS
       ========================= */

    /** Hosting login (masalan: db_user_xxx) */
    @Column(nullable = false, length = 100)
    private String login;

    /** BCrypt hash (hech qachon plain text emas) */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /* =========================
       LIFECYCLE
       ========================= */

    @PrePersist
    @PreUpdate
    private void calculateDates() {
        if (status == null) {
            status = OrderStatus.PENDING;
        }

        if (startTime == null) {
            startTime = LocalDateTime.now();
        }

        if (durationDays == null || durationDays <= 0) {
            throw new IllegalStateException("durationDays must be positive");
        }

        endTime = startTime.plusDays(durationDays);
    }

    /* =========================
       HELPERS
       ========================= */

    @Transient
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endTime);
    }

    @Transient
    public long remainingDays() {
        return java.time.Duration.between(LocalDateTime.now(), endTime).toDays();
    }
}
