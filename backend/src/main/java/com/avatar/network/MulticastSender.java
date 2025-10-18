package com.avatar.network;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

@Component
public class MulticastSender implements ApplicationRunner {
    private static final String MULTICAST_ADDRESS = "230.0.0.1"; // grupo multicast
    private static final int PORT = 4444;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) {
            String ip = InetAddress.getLocalHost().getHostAddress();
            byte[] buffer = ip.getBytes();

            DatagramPacket packet = new DatagramPacket(
                    buffer,
                    buffer.length,
                    InetAddress.getByName(MULTICAST_ADDRESS),
                    PORT
            );

            socket.send(packet);

            System.out.println("📡 Multicast enviado por: " + ip);
        } catch (Exception e) {
            System.out.println("❌ Erro ao enviar multicast: " + e);
        }
    }
}
