package ch.heigvd.emitters;

import picocli.CommandLine;

import java.text.SimpleDateFormat;
import java.util.concurrent.Callable;

public abstract class AbstractEmitter implements Callable<Integer> {
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

    protected SimpleDateFormat dateFormat;

    public AbstractEmitter() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
