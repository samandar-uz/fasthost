package org.example.fasthost.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.fasthost.entity.abs.BaseEntity;
import org.example.fasthost.entity.enums.OrderStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Orders extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariffs tariff;

    @Column(nullable = false)
    private Integer durationMonths;   // tarif muddati (1, 3, 12 oy)

    @Column(nullable = false)
    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column
    private String domainName;

    @Column
    private String paymentId; // to'lov provayderidan kelgan ID

    // Hosting uchun generatsiya qilingan DB ma’lumotlari
    @Column
    private String databaseName;

    @Column
    private String databaseUser;

    @Column
    private String databasePassword;

    @Column
    private Integer cronJobsUsed;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = OrderStatus.PENDING; // default holat
        }
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        if (durationMonths != null && endTime == null) {
            endTime = startTime.plusMonths(durationMonths);
        }
    }
}
