package ch.heigvd;

import picocli.CommandLine;
import picocli.CommandLine.Command;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.Scanner;
import java.net.SocketTimeoutException;

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

    private static final int TIMEOUT_MS = 3000; // 3 seconds timeout

    @Override
    public Integer call() {

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);
            String myself = InetAddress.getLocalHost().getHostAddress() + ":" + parent.getPort();
            System.out.println("Unicast emitter started (" + myself + ")");

            InetAddress serverAddress = InetAddress.getByName(host);

            Scanner scanner = new Scanner(System.in);

            while (true) {

                // Display the menu
                System.out.println("Menu:");
                System.out.println("1. GET-LAST (Get the last position of an ID)");
                System.out.println("2. GET-HISTORY (Get the history of positions for an ID)");
                System.out.println("3. GET-ALL (Get last positions of all trackers)");
                System.out.println("4. GET-IDS (Get the list of IDs on the server)");
                System.out.println("0. Quit");
                System.out.print("Choose an option: ");

                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume the newline after nextInt()

                String message = "<>";

                switch (choice) {
                    case 1:
                        System.out.print("Enter the ID for GET-LAST: ");
                        String lastId = scanner.nextLine();
                        message = "GET-LAST:" + lastId;
                        break;
                    case 2:
                        System.out.print("Enter the ID for GET-HISTORY: ");
                        String historyId = scanner.nextLine();
                        message = "GET-HISTORY:" + historyId;
                        break;
                    case 3:
                        message = "GET-ALL:";
                        break;
                    case 4:
                        message = "GET-IDS:";
                        break;
                    case 0:
                        System.out.println("Program terminated.");
                        socket.close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                        continue;
                }

                // Receive the server's response
                byte[] responseBuffer = new byte[5096];
                DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);

                //System.out.println("Unicasting '" + message + "' to " + host + ":" + parent.getPort());

                byte[] payload = message.getBytes(StandardCharsets.UTF_8);

                DatagramPacket datagram = new DatagramPacket(
                        payload,
                        payload.length,
                        serverAddress,
                        parent.getPort()
                );



                boolean retry = false;
                do {
                    socket.send(datagram);

                    try {
                        socket.receive(responsePacket);

                        String response = new String(responsePacket.getData(), 0, responsePacket.getLength());
                        System.out.println("------------------------------");
                        //System.out.println("Server response: " + response);
                        System.out.println(response);
                        System.out.println("------------------------------");
                        retry = false;
                    } catch (SocketTimeoutException e) {
                        System.out.println("Timeout: No response received from the server.");
                        if (!retry){
                            retry = true;
                            System.out.println("retry");
                        } else{
                            retry = false;
                            System.out.println("Already retried, stop request");
                        }
                    }
                } while (retry);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }
}
