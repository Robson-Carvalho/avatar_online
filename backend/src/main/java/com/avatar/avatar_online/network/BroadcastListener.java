package com.avatar.avatar_online.network;

import com.avatar.avatar_online.service.PeerService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

@Component
public class BroadcastListener {
    private final PeerService peerService;

    public BroadcastListener(PeerService peerService) {
        this.peerService = peerService;
    }

    @PostConstruct
    public void startListener() {
        new Thread(this::listenForBroadcasts).start();

    }

    private void listenForBroadcasts() {
        try (DatagramSocket socket = new DatagramSocket(4444, InetAddress.getByName("0.0.0.0"))) {
            byte[] buf = new byte[256];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String receivedIp = new String(packet.getData(), 0, packet.getLength()).trim();
                String myIp = Host.getLocalIPv4();

                if (!Host.getAllLocalIPv4s().contains(myIp)) {
                    System.out.println("📥 Recebi broadcast de: "+ receivedIp);
                    peerService.addPeer(receivedIp);
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao escutar broadcasts: " + e);
        }
    }
}
