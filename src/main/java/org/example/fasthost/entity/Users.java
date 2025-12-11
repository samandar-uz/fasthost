package org.example.fasthost.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.fasthost.entity.abs.BaseEntity;
import org.example.fasthost.entity.enums.Role;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(unique = true, nullable = false)
    private String key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;
}
