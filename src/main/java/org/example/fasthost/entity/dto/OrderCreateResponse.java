package org.example.fasthost.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class OrderCreateResponse {

    private Long orderId;

    private BigDecimal totalPrice;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer durationDays;
}
