package com.faiz.videostreaming.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class apiResponse<T> {
    private String message;
    private boolean status;
    private T data;               // Generic type for more type safety
    private int statusCode;       // HTTP status code
    private LocalDateTime timestamp;  // Response timestamp

    // Factory method to create a successful response
    public static <T> apiResponse<T> success(String message, T data, int statusCode) {
        return apiResponse.<T>builder()
                .message(message)
                .status(true)
                .data(data)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Factory method to create an error response
    public static <T> apiResponse<T> error(String message, int statusCode) {
        return apiResponse.<T>builder()
                .message(message)
                .status(false)
                .statusCode(statusCode)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
