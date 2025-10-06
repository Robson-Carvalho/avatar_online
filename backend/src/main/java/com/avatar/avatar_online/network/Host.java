package com.avatar.avatar_online.network;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Host {
    private Host() {
        throw new UnsupportedOperationException("Classe utilitária");
    }

    public static List<String> getLocalNetworkHosts() {
        List<String> hosts = new ArrayList<>();
        String localIP = getLocalIPv4();

        if (localIP == null || !localIP.contains(".")) {
            System.err.println("IP local inválido.");
            return hosts;
        }

        String networkPrefix = localIP.substring(0, localIP.lastIndexOf("."));

        for (int i = 1; i <= 20; i++) {
            String host = networkPrefix + "." + i;
            hosts.add(host);
        }

        return hosts;
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