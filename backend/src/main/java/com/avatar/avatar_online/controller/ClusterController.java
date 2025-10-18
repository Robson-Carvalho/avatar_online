package com.avatar.avatar_online.controller;

import com.avatar.avatar_online.raft.model.LeaderInfo;
import com.avatar.avatar_online.raft.service.ClusterLeadershipService;
import com.avatar.avatar_online.raft.service.LeaderDiscoveryService;
import com.avatar.avatar_online.raft.service.LeaderRegistryService;
import com.avatar.avatar_online.raft.service.DatabaseSyncService;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cluster")
public class ClusterController {

    private final HazelcastInstance hazelcast;
    private final ClusterLeadershipService leadershipService;
    private final LeaderDiscoveryService leaderDiscoveryService;
    private final LeaderRegistryService leaderRegistryService;

    public ClusterController(@Qualifier("hazelcastInstance") HazelcastInstance hazelcast,
                             ClusterLeadershipService leadershipService,
                             LeaderDiscoveryService leaderDiscoveryService,
                             LeaderRegistryService leaderRegistryService) {
        this.hazelcast = hazelcast;
        this.leadershipService = leadershipService;
        this.leaderDiscoveryService = leaderDiscoveryService;
        this.leaderRegistryService = leaderRegistryService;
    }

    @GetMapping("/status")
    public Map<String, Object> getClusterStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("isLeader", leadershipService.isLeader());
        status.put("leaderInfo", leadershipService.getLeaderInfo());
        status.put("clusterSize", leadershipService.getClusterSize());
        status.put("nodeInfo", leaderDiscoveryService.getCurrentNodeInfo());
        status.put("timestamp", System.currentTimeMillis());

        LeaderInfo leader = leaderRegistryService.getCurrentLeader();
        if (leader != null) {
            status.put("registeredLeader", leader.toString());
        } else {
            status.put("registeredLeader", "Nenhum líder registrado");
        }

        return status;
    }

    @GetMapping("/leader")
    public Map<String, Object> getLeaderInfo() {
        Map<String, Object> leaderInfo = new HashMap<>();

        LeaderInfo leader = leaderRegistryService.getCurrentLeader();
        if (leader != null) {
            leaderInfo.put("nodeId", leader.getNodeId());
            leaderInfo.put("host", leader.getHost());
            leaderInfo.put("port", leader.getPort());
            leaderInfo.put("httpAddress", leader.getHttpAddress());
            leaderInfo.put("lastHeartbeat", leader.getLastHeartbeat());
            leaderInfo.put("isExpired", leader.isExpired(45000));
        } else {
            leaderInfo.put("error", "Líder não encontrado");
        }

        return leaderInfo;
    }

    @GetMapping("/nodes")
    public Map<String, Object> getClusterNodes() {
        Map<String, Object> nodes = new HashMap<>();
        nodes.put("totalNodes", leadershipService.getClusterSize());
        nodes.put("currentNode", leaderDiscoveryService.getCurrentNodeInfo());
        return nodes;
    }

    @GetMapping("/election/info")
    public Map<String, Object> getElectionInfo() {
        Map<String, Object> info = new HashMap<>();

        IMap<String, String> electionMap = hazelcast.getMap("leader-election");
        String electionLeader = electionMap.get("current-leader-node");

        info.put("currentNodeId", leadershipService.getCurrentNodeId());
        info.put("electionLeader", electionLeader);
        info.put("hasActiveLeader", leadershipService.hasActiveLeader());
        info.put("isLeader", leadershipService.isLeader());
        info.put("clusterSize", leadershipService.getClusterSize());

        return info;
    }

    @PostMapping("/election/resign")
    public ResponseEntity<?> resignLeadership() {
        try {
            if (leadershipService.isLeader()) {
                IMap<String, String> electionMap = hazelcast.getMap("leader-election");
                electionMap.remove("current-leader-node");
                return ResponseEntity.ok("{\"message\": \"Liderança renunciada\"}");
            } else {
                return ResponseEntity.badRequest()
                        .body("{\"error\": \"Este nó não é o líder\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Erro ao renunciar: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/sync/status")
    public ResponseEntity<?> getSyncStatus() {
        try {
            IMap<String, Object> syncMap = hazelcast.getMap("sync-markers");
            Long lastSync = (Long) syncMap.get("last-sync-timestamp");

            Map<String, Object> status = new HashMap<>();
            status.put("lastSyncTimestamp", lastSync);
            status.put("lastSyncHuman", lastSync != null ?
                    Instant.ofEpochMilli(lastSync)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    : "Nunca");
            status.put("hasData", syncMap.get("user-export") != null);
            status.put("isLeader", leadershipService.isLeader());

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Erro ao obter status: " + e.getMessage() + "\"}");
        }
    }
}