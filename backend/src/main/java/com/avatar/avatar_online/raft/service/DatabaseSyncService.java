package com.avatar.avatar_online.raft.service;

import com.avatar.avatar_online.models.User;
import com.avatar.avatar_online.raft.Logs.OpenPackCommand;
import com.avatar.avatar_online.raft.Logs.UserSignUpCommand;
import com.avatar.avatar_online.repository.UserRepository;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Set;
import java.util.UUID;

@Service
public class DatabaseSyncService {

    private final UserRepository userRepository;
    private final HazelcastInstance hazelcast;

    private static final String SYNC_MAP = "sync-markers";
    private static final String SYNC_MARKER = "last-sync-timestamp";

    public DatabaseSyncService(UserRepository userRepository,
                               @Qualifier("hazelcastInstance") HazelcastInstance hazelcast,
                               WebClient.Builder webClientBuilder) {
        this.userRepository = userRepository;
        this.hazelcast = hazelcast;
    }

    @Transactional
    public void applyUserSignUpCommand(UserSignUpCommand command){
        User newUser = new User(
                command.getName(),
                command.getNickname(),
                command.getEmail(),
                command.getPassword()
        );

        newUser.setId(UUID.fromString(command.getPlayerId()));

        userRepository.save(newUser);
    }

    @Transactional
    public void applyOpenPackCommand(OpenPackCommand command){

    }

    public void propagateUserSignUpCommand(UserSignUpCommand command){
        Set<Member> set = hazelcast.getCluster().getMembers(); // for each para iterar por cima do set


    }

    public void progageteOpenPackCommand(OpenPackCommand command){

    }
}