package com.avatar.avatar_online.publisher_subscriber.handlers;

import com.avatar.avatar_online.DTOs.CardDTO;
import com.avatar.avatar_online.DTOs.UserDTO;
import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.publisher_subscriber.model.OnlineUsers;
import com.avatar.avatar_online.publisher_subscriber.model.OperationRequestDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationResponseDTO;
import com.avatar.avatar_online.publisher_subscriber.model.OperationStatus;
import com.avatar.avatar_online.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class HandleUser {
    private final OnlineUsers onlineUsers;
    private final UserService userService;

    @Autowired
    public HandleUser(OnlineUsers onlineUsers, UserService userService) {
        this.onlineUsers = onlineUsers;
        this.userService = userService;
    }

    public OperationResponseDTO handleAuthUser(OperationRequestDTO operation){
        String userID = (String) operation.getPayload().get("userID");

        try {
            Optional<User> user = userService.findById(UUID.fromString(userID));

            if (user.isPresent()) {
                return new OperationResponseDTO(operation.getOperationType(), OperationStatus.OK, "Usuário autenticado!", true);
            } else {
                return new OperationResponseDTO(operation.getOperationType(), OperationStatus.WARNING,"Usuário não autenticado!", false);
            }
        } catch (Exception e) {
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR,"Erro inesperado: " + e.getMessage(), null
            );
        }
    }

    public OperationResponseDTO handleGetOnlineUsers(OperationRequestDTO operation){
        String userID = (String) operation.getPayload().get("userID");

        try {
            List<User> users = onlineUsers.getOnlineUsers(userID);
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.OK, "Usuários online!", users);
        } catch (Exception e) {
            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR, "Interno erro: "+e.getMessage(), null);
        }
    }


    public OperationResponseDTO handleCreateUser(OperationRequestDTO operation) {
        String name = (String) operation.getPayload().get("name");
        String nickname = (String) operation.getPayload().get("nickname");
        String email = (String) operation.getPayload().get("email");
        String password = (String) operation.getPayload().get("password");

        UserDTO user = new UserDTO(name, nickname, email, password);

        try {
            ResponseEntity<?> response = userService.createUser(user);

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

    public OperationResponseDTO handleLoginUser(OperationRequestDTO operation) {
        String nickname = (String) operation.getPayload().get("nickname");
        String password = (String) operation.getPayload().get("password");

        try {
            Optional<User> user = userService.login(nickname, password);

            if(user.isEmpty()){
                return new OperationResponseDTO(operation.getOperationType(), OperationStatus.ERROR, "E-mail e/ou senha incorretos", null);
            }

            return new OperationResponseDTO(operation.getOperationType(), OperationStatus.OK, "Login realizado com sucesso!", user);

        } catch (Exception e) {
            return new OperationResponseDTO(operation.getOperationType(),OperationStatus.ERROR, "Erro inesperado: " + e.getMessage(), null);
        }
    }
}
