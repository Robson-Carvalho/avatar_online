package com.avatar.avatar_online.controller;

import com.avatar.avatar_online.DTOs.UserDTO;
import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.service.UserService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDTO user) {
        try {
            return userService.createUser(user);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Erro ao processar requisição: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Falha ao buscar usuários: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable UUID id) {
        try {
            Optional<User> user = userService.findById(id);
            return user.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Erro ao buscar usuário: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/count")
    public ResponseEntity<?> getUserCount() {
        try {
            long count = userService.count();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Erro ao contar usuários: " + e.getMessage() + "\"}");
        }
    }
}
