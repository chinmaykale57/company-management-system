package com.example.sellerhelp.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDto<T> {

    private boolean success;
    private String message;
    private T data;
    private HttpStatus status;
    private ZonedDateTime timestamp;

    // QUICK SUCCESS
    public static <T> ApiResponseDto<T> ok(T data) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .status(HttpStatus.OK)
                .timestamp(ZonedDateTime.now())
                .build();
    }

    public static <T> ApiResponseDto<T> ok(T data, String message) {
        return ApiResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(HttpStatus.OK)
                .timestamp(ZonedDateTime.now())
                .build();
    }


    // ERROR
    public static <T> ApiResponseDto<T> error(String message, HttpStatus status) {
        return ApiResponseDto.<T>builder()
                .success(false)
                .message(message)
                .status(status)
                .timestamp(ZonedDateTime.now())
                .build();
    }
}