package com.avatar.avatar_online.p2p;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NodeScheduller {

    private final NodeReferences nodeReferences;
    private final NodeClient nodeClient;

    private final String selfPeerURL;
    private final String peerID;

    public NodeScheduller(NodeReferences nodeReferences, NodeClient nodeClient, @Value("${node.base-url}")String selfPeerURL,
    @Value("${node.peer}") String peerID) {
        this.nodeReferences = nodeReferences;
        this.nodeClient = nodeClient;
        this.selfPeerURL = selfPeerURL;
        this.peerID = peerID;
    }

    @Scheduled(fixedRate = 3000)
    public void sendData(){
        for(Node node : nodeReferences.getNodes()){
            if(node.getBaseURL().equals(selfPeerURL)){
               continue;
            }
            try {
                nodeClient.SendData(node.getBaseURL() + "/nodes/data", "Olá peer " + node.getNodeId() + " - - - sou o peer: " + peerID);
            }
            catch(Exception e){
                System.out.println("Error sending data" + node.getBaseURL() + ": " + e.getMessage());
            }
        }
    }
}
