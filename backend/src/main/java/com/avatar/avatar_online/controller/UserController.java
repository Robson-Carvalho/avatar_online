package com.avatar.avatar_online.controller;

import com.avatar.avatar_online.models.UserEntity;
import com.avatar.avatar_online.service.LeaderDiscoveryService;
import com.avatar.avatar_online.service.UserService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final LeaderDiscoveryService leaderDiscoveryService;
    private final RestTemplate restTemplate;

    public UserController(UserService userService,
                          LeaderDiscoveryService leaderDiscoveryService,
                          RestTemplate restTemplate) {
        this.userService = userService;
        this.leaderDiscoveryService = leaderDiscoveryService;
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserEntity user) {
        try {
            // Tenta criar localmente (se for líder)
            UserEntity createdUser = userService.createUser(user);
            return ResponseEntity.ok(createdUser);

        } catch (IllegalStateException e) {
            // Não é líder - redireciona para o líder
            return redirectToLeader("/api/users", user, HttpMethod.POST);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\": \"Erro interno do servidor: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        List<UserEntity> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUserCount() {
        long count = userService.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable UUID id) {
        Optional<UserEntity> user = userService.findById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserEntity> getUserByEmail(@PathVariable String email) {
        Optional<UserEntity> user = userService.findByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<UserEntity> getUserByNickname(@PathVariable String nickname) {
        Optional<UserEntity> user = userService.findByNickname(nickname);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/check/email/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/check/nickname/{nickname}")
    public ResponseEntity<Boolean> checkNicknameExists(@PathVariable String nickname) {
        boolean exists = userService.nicknameExists(nickname);
        return ResponseEntity.ok(exists);
    }

    /**
     * Redireciona a requisição para o nó líder
     */
    private ResponseEntity<?> redirectToLeader(String path, Object body, HttpMethod method) {
        try {
            Optional<String> leaderAddress = leaderDiscoveryService.getLeaderHttpAddress();

            if (leaderAddress.isEmpty()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("{\"error\": \"Líder não encontrado no cluster. Tente novamente.\"}");
            }

            String leaderUrl = leaderAddress.get() + path;
            System.out.println("🔄 Redirecionando para líder: " + leaderUrl);

            // Cria headers para o redirecionamento
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);

            // Faz a requisição para o líder
            ResponseEntity<String> response = restTemplate.exchange(
                    leaderUrl, method, requestEntity, String.class
            );

            // Retorna a resposta do líder
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .body(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("{\"error\": \"Falha no redirecionamento para o líder: " + e.getMessage() + "\"}");
        }
    }
}