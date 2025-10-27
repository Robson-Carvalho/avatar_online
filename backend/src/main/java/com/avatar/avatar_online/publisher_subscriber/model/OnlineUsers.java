package com.avatar.avatar_online.publisher_subscriber.model;

import com.avatar.avatar_online.models.User;
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

    private static final String ONLINE_USERS_MAP = "online-users";

    public OnlineUsers(@Qualifier("hazelcastInstance") HazelcastInstance hazelcast, UserService userService) {
        this.onlineUsers = hazelcast.getMap(ONLINE_USERS_MAP);
        this.userService = userService;
    }

    public void addUser(String userId, String sessionId) {
        onlineUsers.put(userId, sessionId);
    }

    public void removeByUserId(String userId) {
        onlineUsers.remove(userId);
    }

    public void removeBySessionId(String sessionId) {
        Optional<String> userId = getUserIdBySessionId(sessionId);
        userId.ifPresent(onlineUsers::remove);
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

    public List<User> getOnlineUsers(String userID) {
        UUID excludedUserId = UUID.fromString(userID);

        return this.getAllUserIds()
                .stream()
                .map(idStr -> {
                    try {
                        UUID id = UUID.fromString(idStr);
                        return userService.findById(id).orElse(null);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(user -> !user.getId().equals(excludedUserId)) // Filtra o usuário excluído
                .collect(Collectors.toList());
    }

    public int countOnlineUsers() {
        return onlineUsers.size();
    }
}
