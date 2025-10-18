package com.avatar.network;

import com.avatar.service.PeerService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

@Component
public class MulticastListener {
    private final PeerService peerService;

    private static final String MULTICAST_ADDRESS = "230.0.0.1"; // grupo multicast
    private static final int PORT = 4444;

    public MulticastListener(PeerService peerService) {
        this.peerService = peerService;
    }

    @PostConstruct
    public void startListener() {
        new Thread(this::listenForMulticast).start();
    }

    private void listenForMulticast() {
        try (MulticastSocket socket = new MulticastSocket(PORT)) {
            InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(group);

            byte[] buf = new byte[256];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String receivedIp = new String(packet.getData(), 0, packet.getLength()).trim();
                String myIp = InetAddress.getLocalHost().getHostAddress();

                if (!receivedIp.equals(myIp)) {
                    System.out.println("📥 Recebi multicast de: " + receivedIp);
                    peerService.addPeer(receivedIp);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Erro ao escutar multicast: " + e);
        }
    }
}
