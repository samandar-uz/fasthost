package org.example.fasthost.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCreateRequest {
    private Integer tariffId;
    private Integer durationMonths; // 1/3/6/12
    private String domainName;      // optional
}
