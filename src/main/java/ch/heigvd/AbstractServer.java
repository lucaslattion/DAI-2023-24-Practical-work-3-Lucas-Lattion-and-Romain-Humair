package ch.heigvd;

import picocli.CommandLine;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.util.concurrent.Callable;

public abstract class AbstractServer implements Callable<Integer> {
    @CommandLine.ParentCommand
    protected ch.heigvd.Main parent;

    @CommandLine.Option(
            names = {"-H", "--host"},
            description = "Subnet range/multicast address to use.",
            required = true,
            scope = CommandLine.ScopeType.INHERIT
    )
    protected String host;

}
