package net.result.sandnode.util;

import org.jetbrains.annotations.NotNull;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetworkUtil {
    public static @NotNull String replaceZeroes(@NotNull Endpoint endpoint, int defaultPort) {
        String host = endpoint.host();
        try {
            if (endpoint.host().equals("0.0.0.0")) {
                String localIP = getLocalIP();
                host = localIP.contains(":")
                        ? "[%s]".formatted(localIP)
                        : localIP;
            }
        } catch (SocketException ignored) {
        }
        return new Endpoint(host, endpoint.port()).toString(defaultPort);
    }

    public static String getLocalIP() throws SocketException {
        List<InetAddress> list = new ArrayList<>();
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = networkInterfaces.nextElement();
            if (netInterface.isUp()) {
                Enumeration<InetAddress> inetAddresses = netInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        list.add(inetAddress);
                    }
                }
            }
        }

        if (!list.isEmpty()) {
            for (InetAddress inetAddress : list)
                if (inetAddress instanceof Inet4Address)
                    return inetAddress.getHostAddress();
            return list.get(0).getHostAddress().split("%")[0];
        }

        return InetAddress.getLoopbackAddress().getHostAddress();
    }
}
