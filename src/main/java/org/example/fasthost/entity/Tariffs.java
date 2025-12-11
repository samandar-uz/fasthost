package org.example.fasthost.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.example.fasthost.entity.abs.BaseEntity;

@Entity
@Table(name = "tariffs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tariffs extends BaseEntity {


    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer limitQuota;

    @Column(nullable = false)
    private Integer limitMemory;

    @Column(nullable = false)
    private Boolean unlimitedTraffic;

    @Column
    private Integer trafficLimit;

    @Column(nullable = false)
    private Integer limitDomains;

    @Column(nullable = false)
    private Integer limitFtpUsers;

    @Column(nullable = false)
    private Integer limitEmails;

    @Column(nullable = false)
    private Integer limitDb;

    @Column(nullable = false)
    private Integer limitScheduler;

    @Column(nullable = false)
    private Boolean active = true;
}
