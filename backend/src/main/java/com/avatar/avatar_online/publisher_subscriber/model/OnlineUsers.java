package com.avatar.avatar_online.publisher_subscriber.model;

import com.avatar.avatar_online.publisher_subscriber.service.Communication;
import com.avatar.avatar_online.service.UserService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OnlineUsers {
    // <userId, sessionId>
    private final IMap<String, String> onlineUsers;
    private final UserService  userService;
    private final Communication communication;


    private static final String ONLINE_USERS_MAP = "online-users";

    public OnlineUsers(@Qualifier("hazelcastInstance") HazelcastInstance hazelcast, UserService userService, Communication communication) {
        this.onlineUsers = hazelcast.getMap(ONLINE_USERS_MAP);
        this.userService = userService;
        this.communication = communication;
    }

    private void notifyNewStateOnlineUsers() {
        if (!onlineUsers.isEmpty()) {
            for (String sessionId : onlineUsers.values()) {
                OperationResponseDTO response = new OperationResponseDTO(
                        OperationType.GET_ONLINE_USERS.toString(),
                        OperationStatus.OK, "Usuários online!",
                        this.getOnlineUsers()
                );

                communication.sendToUser(sessionId, response);
            }
        }
    }


    public void addUser(String userId, String sessionId) {
        onlineUsers.put(userId, sessionId);
        this.notifyNewStateOnlineUsers();
    }

    public void removeByUserId(String userId) {
        if(onlineUsers.containsKey(userId)){
            onlineUsers.remove(userId);
            this.notifyNewStateOnlineUsers();
        }
    }

    public void removeBySessionId(String sessionId) {
        Optional<String> userId = getUserIdBySessionId(sessionId);
        userId.ifPresent(onlineUsers::remove);
        this.notifyNewStateOnlineUsers();
    }

    public Optional<String> getSessionIdByUserId(String userId) {
        return Optional.ofNullable(onlineUsers.get(userId));
    }

    public Optional<String> getUserIdBySessionId(String sessionId) {
        return onlineUsers.entrySet()
                .stream()
                .filter(entry -> sessionId.equals(entry.getValue()))
                .map(java.util.Map.Entry::getKey)
                .findFirst();
    }

    public boolean isUserOnline(String userId) {
        return onlineUsers.containsKey(userId);
    }

    public boolean isSessionActive(String sessionId) {
        return onlineUsers.containsValue(sessionId);
    }

    private Set<String> getAllUserIds() {
        return onlineUsers.keySet();
    }

    public List<OnlineUserDTO> getOnlineUsers() {
        return this.getAllUserIds()
                .stream()
                .filter(Objects::nonNull)
                .map(idStr -> {
                    try {
                        UUID id = UUID.fromString(idStr);
                        return userService.findById(id).orElse(null);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(user -> new OnlineUserDTO(user.getId().toString(), user.getNickname()))
                .collect(Collectors.toList());
    }

    public int countOnlineUsers() {
        return onlineUsers.size();
    }
}
