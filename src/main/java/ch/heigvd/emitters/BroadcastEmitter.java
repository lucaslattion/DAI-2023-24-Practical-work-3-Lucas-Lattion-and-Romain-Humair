package ch.heigvd.emitters;

import picocli.CommandLine.Command;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Command(
        name = "broadcast-emitter",
        description = "Start an UDP broadcast emitter"
)
public class BroadcastEmitter extends AbstractEmitter {

    @Override
    public Integer call() {
        try (DatagramSocket socket = new DatagramSocket()) {
            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + parent.getPort();
            System.out.println("Broadcast emitter started (" + myself + ")");

            socket.setBroadcast(true);
            InetAddress broadcastAddress = InetAddress.getByName(host);

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    String timestamp = dateFormat.format(new Date());
                    String message = "Hello, from broadcast emitter! (" + myself + " at " + timestamp + ")";

                    System.out.println("Broadcasting '" + message + "' to " + host + ":" + parent.getPort());

                    byte[] payload = message.getBytes(StandardCharsets.UTF_8);

                    DatagramPacket datagram = new DatagramPacket(
                            payload,
                            payload.length,
                            broadcastAddress,
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
