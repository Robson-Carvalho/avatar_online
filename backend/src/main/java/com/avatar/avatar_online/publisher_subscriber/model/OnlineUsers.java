package com.avatar.avatar_online.publisher_subscriber.model;

import com.avatar.avatar_online.publisher_subscriber.service.Communication;
import com.avatar.avatar_online.raft.service.RedirectService;
import com.avatar.avatar_online.service.UserService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OnlineUsers {
    // <userId, OnlineUserInfo>
    private final IMap<String, OnlineUserInfo> onlineUsers;
    private final UserService  userService;
    private final Communication communication;
    private final HazelcastInstance hazelcast;
    private final RedirectService redirectService;

    private static final String ONLINE_USERS_MAP = "online-users";

    public OnlineUsers(@Qualifier("hazelcastInstance") HazelcastInstance hazelcast, UserService userService, Communication communication, RedirectService redirectService) {
        this.onlineUsers = hazelcast.getMap(ONLINE_USERS_MAP);
        this.userService = userService;
        this.communication = communication;
        this.hazelcast = hazelcast;
        this.redirectService = redirectService;
    }

    private void notifyNewStateOnlineUsers() {
        String currentNodeId = hazelcast.getCluster().getLocalMember().getAddress().getHost();

        if (!onlineUsers.isEmpty()) {
            for (OnlineUserInfo info : onlineUsers.values()) {
                String sessionId = info.getSessionId();
                OperationResponseDTO response = new OperationResponseDTO(
                        OperationType.GET_ONLINE_USERS.toString(),
                        OperationStatus.OK,
                        "Usuários online!",
                        this.getOnlineUsers()
                );

                // sessão não é local
                if(!Objects.equals(currentNodeId, info.getHost())){
                    redirectService.sendOperationResponseToNode(
                            info.getHost(),
                            "update/online/users",
                            response,
                            HttpMethod.POST);
                }else{
                    communication.sendToUser(sessionId, response);
                }
            }
        }
    }

    public void handleUpdateOnlineUsersOtherNode(OperationResponseDTO response){
        String currentNodeId = hazelcast.getCluster().getLocalMember().getAddress().getHost();


        if (!onlineUsers.isEmpty()) {
            for (OnlineUserInfo info : onlineUsers.values()) {
                String sessionId = info.getSessionId();

                if(Objects.equals(currentNodeId, info.getHost())){
                    communication.sendToUser(sessionId, response);
                }
            }}
    }

    public void addUser(String userId, String sessionId, String host) {
        onlineUsers.put(userId, new OnlineUserInfo(sessionId, host));
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
        return Optional.ofNullable(onlineUsers.get(userId)).map(OnlineUserInfo::getSessionId);
    }

    public Optional<OnlineUserInfo> getUserInfoByUserId(String userId) {
        return Optional.ofNullable(onlineUsers.get(userId));
    }

    public Optional<String> getHostByUserId(String userId) {
        return Optional.ofNullable(onlineUsers.get(userId)).map(OnlineUserInfo::getHost);
    }

    public Optional<String> getUserIdBySessionId(String sessionId) {
        return onlineUsers.entrySet()
                .stream()
                .filter(entry -> sessionId.equals(entry.getValue().getSessionId()))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public boolean isUserOnline(String userId) {
        return onlineUsers.containsKey(userId);
    }

    private Set<String> getAllUserIds() {
        return onlineUsers.keySet();
    }

    public List<OnlineUserDTO> getOnlineUsers() {
        return onlineUsers.entrySet()
                .stream()
                .map(entry -> {
                    String userIdStr = entry.getKey();
                    OnlineUserInfo info = entry.getValue();
                    try {
                        UUID id = UUID.fromString(userIdStr);
                        return userService.findById(id)
                                .map(user -> new OnlineUserDTO(user.getId().toString(), user.getNickname(), info.getSessionId(),info.getHost()))
                                .orElse(null);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public int countOnlineUsers() {
        return onlineUsers.size();
    }
}
