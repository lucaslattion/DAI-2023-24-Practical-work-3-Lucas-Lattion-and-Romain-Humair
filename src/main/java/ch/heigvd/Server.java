package ch.heigvd;

import picocli.CommandLine;

import picocli.CommandLine.Command;


import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.net.*;

import java.util.Map;
import java.util.TreeMap;


@CommandLine.Command(
        name = "server",
        description = "Start an UDP server (unicast and multicast)"
)
public class Server extends AbstractServer {

    private static final int NUMBER_OF_THREADS = 5;

    @CommandLine.Option(
            names = {"-i", "--interface"},
            description = "Interface to use",
            scope = CommandLine.ScopeType.INHERIT,
            required = true
    )
    private String interfaceName;

    @CommandLine.Option(
            names = {"-pm", "--port_multicast"},
            description = "port for multicast (default : 5050)",
            scope = CommandLine.ScopeType.INHERIT,
            defaultValue = "5050",
            required = true
    )
    private Integer multicast_port;

    @CommandLine.Option(
            names = {"-pu", "--port_unicast"},
            description = "port for unicast (default : 5051)",
            scope = CommandLine.ScopeType.INHERIT,
            defaultValue = "5051",
            required = true
    )
    private Integer unicast_port;




    private static final Map<String, TreeMap<Long, TrackerData>> trackerDataMap = new TreeMap<>();

    @Override
    public Integer call() {
        ExecutorService executorService = Executors.newFixedThreadPool(2); // The number of threads in the pool must be the same as the number of tasks you want to run in parallel

        try {
            executorService.submit(this::multicast_receiver); // Start the first task
            executorService.submit(this::unicast_receiver); // Start the second task

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

        try (MulticastSocket socket = new MulticastSocket(multicast_port)) {

            // This is new - the executor service has a pool of threads
            executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + multicast_port;
            System.out.println("Multicast receiver started (" + myself + ")");

            InetAddress multicastAddress = InetAddress.getByName(host);
            InetSocketAddress group = new InetSocketAddress(multicastAddress, multicast_port);
            NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
            socket.joinGroup(group, networkInterface);

            byte[] receiveData = new byte[2048];

            while (true) {
                DatagramPacket packet = new DatagramPacket(
                        receiveData,
                        receiveData.length
                );

                socket.receive(packet);

                // This is new - we submit a new task to the executor service
                executor.submit(new MulticastHandler(packet, myself));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    static class MulticastHandler implements Runnable {
        private final DatagramPacket packet;
        private final String myself;

        public MulticastHandler(DatagramPacket packet, String myself) {
            this.packet = packet;
            this.myself = myself;
        }

        @Override
        public void run() {
            try {
                String message = new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );

                System.out.println("Multicast receiver (" + myself + ") received message: " + message);

                if (message.startsWith("PROVIDE:")) {
                    String[] parts = message.substring(8).split(",");

                    if (parts.length == 5) {

                            String trackerId = parts[0];
                            long timestamp = Long.parseLong(parts[1]);
                            double latitude = Double.parseDouble(parts[2]);
                            double longitude = Double.parseDouble(parts[3]);
                            int batteryLevel = Integer.parseInt(parts[4]);

                            trackerDataMap.computeIfAbsent(trackerId, k -> new TreeMap<>())
                                    .put(timestamp, new TrackerData(trackerId, timestamp, latitude, longitude, batteryLevel));

                            System.out.println("Data received from tracker : " + trackerDataMap.get(trackerId).lastEntry().getValue());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }



            //System.out.println("Going to sleep for 10 seconds...");
            // Sleep for a while to simulate a long-running task
            //try {
            //    Thread.sleep(10000);
            //} catch (InterruptedException e) {
            //    e.printStackTrace();
            //}
            //System.out.println("End of sleep");
        }
    }

    public Integer unicast_receiver() {
        // ...

        ExecutorService executor = null;

        try (DatagramSocket socket = new DatagramSocket(unicast_port)) {
            // This is new - the executor service has a pool of threads
            executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + unicast_port;
            System.out.println("Unicast receiver started (" + myself + ")");

            byte[] receiveData = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(
                        receiveData,
                        receiveData.length
                );

                socket.receive(packet);

                // This is new - we submit a new task to the executor service
                executor.submit(new UnicastHandler(packet, myself, socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    static class UnicastHandler implements Runnable {
        private final DatagramPacket packet;
        private final String myself;
        private final DatagramSocket socket;

        public UnicastHandler(DatagramPacket packet, String myself, DatagramSocket socket) {
            this.packet = packet;
            this.myself = myself;
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                String message = new String(
                        packet.getData(),
                        packet.getOffset(),
                        packet.getLength(),
                        StandardCharsets.UTF_8
                );
                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                System.out.println("Unicast receiver (" + myself + ") received message: " + message);

                if (message.startsWith("GET-LAST:")) {
                    String[] parts = message.split(":");
                    if (parts.length == 2) {
                        String requestType = parts[0];
                        String trackerId = parts[1];

                        TrackerData trackerData = getLatestTrackerData(trackerId);
                        if (trackerData != null) {
                            String response = trackerData.toString();
                            sendResponse(response, clientAddress, clientPort, socket);
                        } else {
                            // ID not found, error message
                            String errorResponse = "Error 1: Tracker with ID '" + trackerId + "' not found.";
                            sendResponse(errorResponse, clientAddress, clientPort, socket);
                        }
                    } else {
                        String errorResponse = "Error 3: Invalid format";
                        sendResponse(errorResponse, clientAddress, clientPort, socket);
                    }
                } else if (message.startsWith("GET-HISTORY:")) {
                    String[] parts = message.split(":");
                    if (parts.length == 2) {
                        String requestType = parts[0];
                        String trackerId = parts[1];

                        TrackerData trackerData = getLatestTrackerData(trackerId);
                        if (trackerData != null) {
                            String response = getTrackerHistory(trackerId);
                            sendResponse(response, clientAddress, clientPort, socket);
                        } else {
                            // ID not found, error message
                            String errorResponse = "Error 1: Tracker with ID '" + trackerId + "' not found.";
                            sendResponse(errorResponse, clientAddress, clientPort, socket);
                        }
                    } else {
                        String errorResponse = "Error 3: Invalid format";
                        sendResponse(errorResponse, clientAddress, clientPort, socket);
                    }
                } else if (message.startsWith("GET-ALL:")) {
                    StringBuilder response = new StringBuilder("ALL:\n");
                    if (trackerDataMap.isEmpty()) {
                        response.append("Error 2: empty, no tracker data, start a tracker-gps.\n");
                    } else {
                        //The Map trackerDataMap has data
                        for (Map.Entry<String, TreeMap<Long, TrackerData>> entry : trackerDataMap.entrySet()) {
                            String currentTrackerId = entry.getKey();
                            TrackerData latestTrackerData = entry.getValue().lastEntry().getValue();
                            response.append(currentTrackerId).append(": ").append(latestTrackerData).append("\n");
                        }
                    }
                    sendResponse(response.toString(), clientAddress, clientPort, socket);
                } else if (message.startsWith("GET-IDS:")) {
                    StringBuilder response = new StringBuilder("IDS:\n");
                    if (trackerDataMap.isEmpty()) {
                        response.append("Error 2: empty, no tracker data, start a tracker-gps.\n");
                    } else {
                        //The Map trackerDataMap has data
                        for (String storedTrackerId : trackerDataMap.keySet()) {
                            response.append(storedTrackerId).append("\n");
                        }
                    }
                    sendResponse(response.toString(), clientAddress, clientPort, socket);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //System.out.println("Going to sleep for 10 seconds...");
            // Sleep for a while to simulate a long-running task
            //try {
            //    Thread.sleep(10000);
            //} catch (InterruptedException e) {
            //    e.printStackTrace();
            //}
            //System.out.println("End of sleep");
        }
    }

    private static TrackerData getLatestTrackerData(String trackerId) {
        TreeMap<Long, TrackerData> trackerDataByTimestamp = trackerDataMap.get(trackerId);
        return (trackerDataByTimestamp != null) ? trackerDataByTimestamp.lastEntry().getValue() : null;
    }

    private static String getTrackerHistory(String trackerId) {
        TreeMap<Long, TrackerData> trackerDataByTimestamp = trackerDataMap.get(trackerId);

        StringBuilder historyResponse = new StringBuilder("HISTORY:\n");

        for (Map.Entry<Long, TrackerData> entry : trackerDataByTimestamp.entrySet()) {
            TrackerData trackerData = entry.getValue();
            historyResponse.append(trackerData.toString()).append("\n");
        }

        return historyResponse.toString();
    }

    private static void sendResponse(String response, InetAddress clientAddress, int clientPort, DatagramSocket socket) {
        try {
            byte[] responseData = response.getBytes(StandardCharsets.UTF_8);
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length,
                    clientAddress, clientPort);
            socket.send(responsePacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


