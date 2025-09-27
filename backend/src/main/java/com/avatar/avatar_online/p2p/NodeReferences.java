package com.avatar.avatar_online.p2p;

import java.util.List;

public class NodeReferences {
    private List<Node> nodes = List.of(
            new Node("server01", "http://localhost:8081"),
            new Node("server02", "http://localhost:8082")
    );

    public List<Node> getNodes() {
        return nodes;
    }
}
