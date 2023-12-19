package ch.heigvd;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.Random;

@Command(
        name = "tracker-gps",
        description = "Start an UDP GPS tracker unicast emitter client"
)
public class TrackerGPS implements Callable<Integer> {
    @CommandLine.ParentCommand
    protected ch.heigvd.Main parent;

    @CommandLine.Option(
            names = {"-H", "--host"},
            description = "Subnet range/multicast address to use.",
            required = true,
            scope = CommandLine.ScopeType.INHERIT
    )
    protected String host;

    @CommandLine.Option(
            names = {"-d", "--delay"},
            description = "Delay before sending the message (in milliseconds) (default: 0).",
            defaultValue = "0"
    )
    protected int delay;

    @CommandLine.Option(
            names = {"-f", "--frequency"},
            description = "Frequency of sending the message (in milliseconds) (default: 10000).",
            defaultValue = "10000"
    )
    protected int frequency;

    @CommandLine.Option(
            names = {"-i", "--interface"},
            description = "Interface to use.",
            scope = CommandLine.ScopeType.INHERIT,
            required = true
    )
    private String interfaceName;

    @CommandLine.Option(
            names = {"-I", "--ID"},
            description = "Tracker ID, should not be 0 (default : random IMEI value) ",
            defaultValue = "0"
    )
    private long id;


    protected SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    @Override
    public Integer call() {
        if (id == 0){
            Random random = new Random();
            id = generateRandomIMEI(random);
        }

        try (MulticastSocket socket = new MulticastSocket(parent.getPort())) {
            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + parent.getPort();
            System.out.println("Tracker GPS Multicast emitter started (" + myself + ") with id " + id);

            InetAddress multicastAddress = InetAddress.getByName(host);
            System.out.println("multicast address is " + multicastAddress);
            InetSocketAddress group = new InetSocketAddress(multicastAddress, parent.getPort());
            NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
            socket.joinGroup(group, networkInterface);

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    //String timestamp = dateFormat.format(new Date());
                    String position = generateRandomPosition();
                    String batteryLevel = generateRandomBatteryLevel();
                    String timestamp = String.valueOf(System.currentTimeMillis());

                    String message = "PROVIDE:" + id + "," + timestamp + "," + position + "," + batteryLevel;

                    System.out.println("Multicasting '" + message + "' to " + host + ":" + parent.getPort() + " on interface " + interfaceName);

                    byte[] payload = message.getBytes(StandardCharsets.UTF_8);

                    DatagramPacket datagram = new DatagramPacket(
                            payload,
                            payload.length,
                            group
                    );

                    System.out.println("Payload size : " + payload.length);

                    socket.send(datagram);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, delay, frequency, TimeUnit.MILLISECONDS);

            // Keep the program running for a while
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

            socket.leaveGroup(group, networkInterface);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }

    private static long generateRandomIMEI(Random random) {
        StringBuilder imeiBuilder = new StringBuilder();

        // The first digit should not be zero, so generate a number between 1 and 9
        imeiBuilder.append(1 + random.nextInt(9));

        // Generating the next 14 digits
        for (int i = 0; i < 14; i++) {
            imeiBuilder.append(random.nextInt(10));
        }

        // Converting the string to long
        return Long.parseLong(imeiBuilder.toString());
    }

    private static String generateRandomPosition() {
        Random rand = new Random();
        double latitude = 40.0 + (rand.nextDouble() * 20.0);
        double longitude = -120.0 + (rand.nextDouble() * 40.0);
        return String.format(Locale.ENGLISH, "%.4f", latitude) + "," + String.format(Locale.ENGLISH, "%.4f", longitude);
    }

    private static String generateRandomBatteryLevel() {
        Random rand = new Random();
        int batteryLevel = rand.nextInt(101); // Niveau de batterie entre 0% et 100%
        return String.valueOf(batteryLevel);
    }
}
