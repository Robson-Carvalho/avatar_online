package com.avatar.avatar_online.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final LocalDateTime timestamp;
    private final int status;
    private final String message;
    private final T data;
    private final String path;

    private ApiResponse(Builder<T> builder) {
        this.timestamp = LocalDateTime.now();
        this.status = builder.status;
        this.message = builder.message;
        this.data = builder.data;
        this.path = builder.path;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public String getPath() { return path; }

    public static class Builder<T> {
        private int status;
        private String message;
        private T data;
        private String path;

        public Builder<T> status(int status) {
            this.status = status;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> path(String path) {
            this.path = path;
            return this;
        }

        public ApiResponse<T> build() {
            return new ApiResponse<>(this);
        }
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new Builder<T>()
                .status(200)
                .message("Operação realizada com sucesso")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return new Builder<T>()
                .status(201)
                .message("Recurso criado com sucesso")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return new Builder<T>()
                .status(status)
                .message(message)
                .build();
    }
}