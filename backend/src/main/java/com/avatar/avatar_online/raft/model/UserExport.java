package com.avatar.avatar_online.raft.model;

import com.avatar.avatar_online.models.User;

import java.util.UUID;

public record UserExport(String id, String name, String nickname, String email, String password) {
    // Construtor padrão necessário para RestTemplate.exchange()
    public UserExport {}

    public static UserExport fromEntity(User entity) {
        return new UserExport(
                entity.getId().toString(),
                entity.getName(),
                entity.getNickname(),
                entity.getEmail(),
                entity.getPassword()
        );
    }

    public User toEntity() {
        User entity = new User();
        entity.setId(UUID.fromString(this.id));
        entity.setName(this.name);
        entity.setNickname(this.nickname);
        entity.setEmail(this.email);
        entity.setPassword(this.password);
        return entity;
    }
}