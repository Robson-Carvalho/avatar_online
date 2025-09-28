package com.avatar.avatar_online.network;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

@Component
public class BroadcastSender implements ApplicationRunner {

    private static final int PORT = 4444;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);

            String message = InetAddress.getLocalHost().getHostAddress();
            byte[] buffer = message.getBytes();

            DatagramPacket packet = new DatagramPacket(
                    buffer,
                    buffer.length,
                    InetAddress.getByName("255.255.255.255"),
                    PORT
            );

            socket.send(packet);
            System.out.println("📡 Broadcast enviado: " + message);
        }
    }
}
