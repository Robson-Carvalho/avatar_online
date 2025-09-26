package com.avatar.avatar_online.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public final class ResponseBuilder {

    private ResponseBuilder() {
        throw new UnsupportedOperationException("Classe utilitária");
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        ApiResponse<T> response = new ApiResponse.Builder<T>()
                .status(200)
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(201).body(ApiResponse.created(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> noContent() {
        return ResponseEntity.noContent().build();
    }

    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        ApiResponse<T> response = ApiResponse.error(400, message);
        return ResponseEntity.badRequest().body(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        ApiResponse<T> response = ApiResponse.error(404, message);
        return ResponseEntity.status(404).body(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> internalError(String message) {
        ApiResponse<T> response = ApiResponse.error(500, message);
        return ResponseEntity.status(500).body(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> build(int status, String message, T data) {
        ApiResponse<T> response = new ApiResponse.Builder<T>()
                .status(status)
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.status(status).body(response);
    }

    public static <T> ResponseEntity<ApiResponse<T>> build(int status, String message, T data, HttpServletRequest request) {

        ApiResponse<T> response = new ApiResponse.Builder<T>()
                .status(status)
                .message(message)
                .data(data)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(response);
    }
}