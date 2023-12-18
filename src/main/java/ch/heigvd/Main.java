package ch.heigvd;

import ch.heigvd.emitters.BroadcastEmitter;
import ch.heigvd.emitters.MulticastEmitter;
import ch.heigvd.emitters.UnicastEmitter;
import ch.heigvd.receivers.BroadcastReceiver;
import ch.heigvd.receivers.MulticastReceiver;
import ch.heigvd.receivers.UnicastReceiver;
import ch.heigvd.utils.ListNetworkInterfaces;
import lombok.Getter;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        description = "Practical content of the Java UDP programming chapter",
        version = "1.0.0",
        subcommands = {
                BroadcastEmitter.class,
                MulticastEmitter.class,
                UnicastEmitter.class,
                BroadcastReceiver.class,
                UnicastReceiver.class,
                MulticastReceiver.class,
                ListNetworkInterfaces.class
        },
        scope = CommandLine.ScopeType.INHERIT,
        mixinStandardHelpOptions = true
)
@Getter
public class Main {

    @CommandLine.Option(
            names = {"-p", "--port"},
            description = "Port to use (default: 9876).",
            defaultValue = "9876",
            scope = CommandLine.ScopeType.INHERIT
    )
    protected int port;

    public static void main(String... args) {
        // Source: https://stackoverflow.com/a/11159435
        String commandName = new java.io.File(
                Main.class.getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .getPath()
        ).getName();

        int exitCode = new CommandLine(new Main())
                .setCommandName(commandName)
                .execute(args);
        System.exit(exitCode);
    }
}
