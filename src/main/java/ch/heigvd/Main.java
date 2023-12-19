package ch.heigvd;

import ch.heigvd.utils.ListNetworkInterfaces;
import ch.heigvd.TrackerGPS;
import ch.heigvd.Server;
import ch.heigvd.Client;
import lombok.Getter;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(
        description = "Practical content of the Java UDP programming chapter",
        version = "1.0.0",
        subcommands = {
                ListNetworkInterfaces.class,
                TrackerGPS.class,
                Server.class,
                Client.class
        },
        scope = CommandLine.ScopeType.INHERIT,
        mixinStandardHelpOptions = true
)
@Getter
public class Main {

    @CommandLine.Option(
            names = {"-p", "--port"},
            description = "Port to use (default: 5050).",
            defaultValue = "5050",
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
