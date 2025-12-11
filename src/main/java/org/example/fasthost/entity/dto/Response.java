package org.example.fasthost.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> Response<T> error(String message) {
        return Response.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}