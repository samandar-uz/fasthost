package org.example.fasthost.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OrderCreateResponse {
    private Integer orderId;
    private Double totalPrice;
    private String paymentUrl;
    private String paymentId;   // Click/Payme/Payze ID
}
