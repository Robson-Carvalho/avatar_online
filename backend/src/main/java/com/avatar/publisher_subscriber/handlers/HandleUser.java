package com.avatar.publisher_subscriber.handlers;

import com.avatar.DTOs.UserDTO;
import com.avatar.models.User;
import com.avatar.publisher_subscriber.model.OperationRequestDTO;
import com.avatar.publisher_subscriber.model.OperationResponseDTO;
import com.avatar.publisher_subscriber.model.OperationStatus;
import com.avatar.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HandleUser {

    private final UserService userService;

    @Autowired
    public HandleUser(UserService userService) {
        this.userService = userService;
    }

    public OperationResponseDTO handleCreateUser(OperationRequestDTO operation) {
        String name = (String) operation.getPayload().get("name");
        String nickname = (String) operation.getPayload().get("nickname");
        String email = (String) operation.getPayload().get("email");
        String password = (String) operation.getPayload().get("password");

        UserDTO user = new UserDTO(name, nickname, email, password);

        try {
            // Lembrar de trocar pelo método real
            ResponseEntity<?> response = userService.createUserFake(user);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new OperationResponseDTO(operation.getOperationType(), OperationStatus.OK, "Usuário criado com sucesso!", response.getBody());
            } else if (response.getStatusCode().is4xxClientError()) {
                return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR, "Falha ao criar usuário: " + response.getBody(), null);
            } else {
                return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR,"Erro interno ao criar usuário.", null
                );
            }
        } catch (Exception e) {
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR,"Erro inesperado: " + e.getMessage(), null
            );
        }
    }

    // Lembrar de atualizar para o fluxo correto
    public OperationResponseDTO handleLoginUser(OperationRequestDTO operation) {
        String nickname = (String) operation.getPayload().get("nickname");
        String password = (String) operation.getPayload().get("password");

        try {

            User user = new User(UUID.randomUUID(), "Elinaldo", nickname, "sparta@gmail.com", password);

            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.OK, "Login realizado com sucesso!", user);

        } catch (Exception e) {
            return new OperationResponseDTO(operation.getOperationType(),OperationStatus.ERROR, "Erro inesperado: " + e.getMessage(), null
            );
        }
    }
}
