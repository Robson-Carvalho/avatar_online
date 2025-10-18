package com.avatar.raft.service;

import com.avatar.raft.model.LeaderInfo;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LeaderDiscoveryService {

    private final LeaderRegistryService leaderRegistryService;
    private final ClusterLeadershipService leadershipService;

    public LeaderDiscoveryService(LeaderRegistryService leaderRegistryService,
                                  ClusterLeadershipService leadershipService) {
        this.leaderRegistryService = leaderRegistryService;
        this.leadershipService = leadershipService;
    }

    /**
     * Retorna o endereço HTTP do líder atual
     */
    public Optional<String> getLeaderHttpAddress() {
        // Se este nó é o líder, retorna seu próprio endereço
        if (leadershipService.isLeader()) {
            LeaderInfo currentLeader = leaderRegistryService.getCurrentLeader();
            if (currentLeader != null) {
                return Optional.of(currentLeader.getHttpAddress());
            }
        }

        // Busca o líder registrado
        LeaderInfo leader = leaderRegistryService.getCurrentLeader();
        if (leader != null && !leader.isExpired(45000)) {
            return Optional.of(leader.getHttpAddress());
        }

        return Optional.empty();
    }

    /**
     * Verifica se o líder está acessível
     */
    public boolean isLeaderAccessible() {
        Optional<String> leaderAddress = getLeaderHttpAddress();
        return leaderAddress.isPresent();
    }

    /**
     * Obtém informações completas do líder
     */
    public LeaderInfo getLeaderInfo() {
        return leaderRegistryService.getCurrentLeader();
    }

    public String getCurrentNodeInfo() {
        LeaderInfo leader = leaderRegistryService.getCurrentLeader();
        String leaderStatus = leadershipService.isLeader() ? "LÍDER" : "SEGUIDOR";
        String leaderAddress = leader != null ? leader.getHttpAddress() : "Não definido";

        return String.format("Nó: %s | Status: %s | Líder: %s",
                leadershipService.isLeader() ? leader.getHttpAddress() : "Local",
                leaderStatus,
                leaderAddress);
    }
}