package ch.heigvd;

import picocli.CommandLine;

import picocli.CommandLine.Command;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.net.*;
import java.nio.charset.StandardCharsets;


@CommandLine.Command(
        name = "server",
        description = "Start an UDP server (unicast and multicast"
)
public class Server extends AbstractServer {

    private static final int NUMBER_OF_THREADS = 5;

    @Override
    public Integer call() {
        ExecutorService executorService = Executors.newFixedThreadPool(2); // The number of threads in the pool must be the same as the number of tasks you want to run in parallel

        try {
            executorService.submit(this::worker1); // Start the first task
            executorService.submit(this::worker2); // Start the second task

            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); // Wait for termination
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        } finally {
            executorService.shutdown();
        }

        return 0;
    }

    public Integer multicast_receiver() {
        ExecutorService executor = null;

        try (DatagramSocket socket = new DatagramSocket(parent.getPort())) {
            // This is new - the executor service has a pool of threads
            executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

            InetAddress multicastAddress = InetAddress.getByName(host);
            InetSocketAddress group = new InetSocketAddress(multicastAddress, parent.getPort());

            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + parent.getPort();
            System.out.println("Multicast receiver started (" + myself + ")");

            socket.joinGroup(group, networkInterface);

            byte[] receiveData = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(
                        receiveData,
                        receiveData.length
                );

                socket.receive(packet);

                // This is new - we submit a new task to the executor service
                executor.submit(new ClientHandler(packet, myself));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    public Integer unicast_receiver() {
        // ...

        return 0;
    }
}


