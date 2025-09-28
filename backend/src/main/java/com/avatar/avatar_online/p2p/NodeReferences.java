package com.avatar.avatar_online.p2p;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NodeReferences {
    private final List<Node> nodes = List.of(
            new Node("server01", "http://localhost:8081"),
            new Node("server02", "http://localhost:8082"),
            new Node("server03", "http://localhost:8083")
    );

    public List<Node> getNodes() {
        return nodes;
    }
}
