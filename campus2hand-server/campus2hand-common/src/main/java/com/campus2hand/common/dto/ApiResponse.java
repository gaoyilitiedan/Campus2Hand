package com.campus2hand.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;
    private String timestamp;
    private String traceId;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = 0;
        response.message = "success";
        response.data = data;
        response.timestamp = Instant.now().toString();
        response.traceId = UUID.randomUUID().toString();
        return response;
    }

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> success(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = 0;
        response.message = message;
        response.timestamp = Instant.now().toString();
        response.traceId = UUID.randomUUID().toString();
        return response;
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.code = code;
        response.message = message;
        response.timestamp = Instant.now().toString();
        response.traceId = UUID.randomUUID().toString();
        return response;
    }

    public static <T> ApiResponse<T> fail(String message) {
        return fail(5000, message);
    }

    public boolean isSuccess() {
        return this.code == 0;
    }
}