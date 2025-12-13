package org.example.fasthost.entity.dto;


import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

@Getter
@Setter
public class OrderCreateRequest {

    private Long tariffId;

    private Integer durationDays;
}
