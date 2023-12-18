package ch.heigvd.utils;

import picocli.CommandLine.Command;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.Callable;

@Command(
        name = "list-network-interfaces",
        description = "List all available network interfaces"
)
public class ListNetworkInterfaces implements Callable<Integer> {

    @Override
    public Integer call() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();

            if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                System.out.println("Network interface: " + networkInterface.getDisplayName());
            }
        }

        return 0;
    }
}