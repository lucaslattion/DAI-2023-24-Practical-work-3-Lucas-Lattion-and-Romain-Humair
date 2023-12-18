package ch.heigvd;

import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Command(
        name = "client",
        description = "Start an UDP GPS tracker unicast emitter client"
)
public class Client implements Callable<Integer> {
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

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    @Override
    public Integer call() {
        try (DatagramSocket socket = new DatagramSocket()) {
            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + parent.getPort();
            System.out.println("Unicast emitter started (" + myself + ")");

            InetAddress serverAddress = InetAddress.getByName(host);
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

            scheduler.scheduleAtFixedRate(() -> {
                try {
                    String timestamp = dateFormat.format(new Date());
                    String message = "Hello, from unicast emitter! (" + myself + " at " + timestamp + ")";

                    System.out.println("Unicasting '" + message + "' to " + host + ":" + parent.getPort());

                    byte[] payload = message.getBytes(StandardCharsets.UTF_8);

                    DatagramPacket datagram = new DatagramPacket(
                            payload,
                            payload.length,
                            serverAddress,
                            parent.getPort()
                    );

                    socket.send(datagram);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, delay, frequency, TimeUnit.MILLISECONDS);

            // Keep the program running for a while
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }

        return 0;
    }
}
