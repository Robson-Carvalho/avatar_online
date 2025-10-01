package com.avatar.avatar_online.network;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Host {
    private Host() {
        throw new UnsupportedOperationException("Classe utilitária");
    }

    public static String getLocalIPv4() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    if (addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Erro ao obter interfaces de rede: " + e.getMessage());
        }

        return "unknown";
    }

    public static List<String> getAllLocalIPv4s() {
        List<String> ips = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                if (iface.isLoopback() || !iface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    if (addr instanceof Inet4Address) {
                        ips.add(addr.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Erro ao obter interfaces de rede: " + e.getMessage());
        }
        return ips;
    }

    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown-host";
        }
    }
}