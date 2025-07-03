package net.result.main;

import net.result.sandnode.chain.Chain;
import net.result.sandnode.exception.ServerClosingException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.link.SandnodeLinkRecord;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.serverclient.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Stream;

public class HubConsole {
    @FunctionalInterface
    interface F {
        void run() throws Exception;
    }

    private static final Logger LOGGER = LogManager.getLogger(HubConsole.class);

    private final SandnodeServer server;
    private final Map<String, F> commands = new HashMap<>();
    public boolean running = true;

    public HubConsole(SandnodeServer server) {
        this.server = server;

        commands.put("exit", this::exit);
        commands.put("getlink", this::getLink);
        commands.put("info", this::info);
        commands.put("sessions", this::sessions);
        commands.put("chains", this::chains);
    }

    public void start() {
        System.out.println("Console started.");
        printAvailable();

        Scanner scanner = new Scanner(System.in);
        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            F command = commands.get(input.toLowerCase());

            if (command != null) {
                try {
                    command.run();
                } catch (Exception e) {
                    LOGGER.error("Command error", e);
                }
            } else {
                System.out.println("Unknown command: " + input);
                printAvailable();
            }
        }

        scanner.close();
    }

    private void printAvailable() {
        System.out.printf("Available commands: %s%n", String.join(", ", commands.keySet()));
    }


    private void exit() throws ServerClosingException {
        System.out.println("Shutting down...");
        running = false;
        server.close();
    }

    private void getLink() throws EncryptionTypeException, KeyStorageNotFoundException {
        URI link = SandnodeLinkRecord.fromServer(server).getURI();
        System.out.println("Link for server:");
        System.out.println();
        System.out.println(link);
        System.out.println();
    }

    private void info() {
        System.out.printf("Server: %s%n", server);
        System.out.printf("Node: %s%n", server.node);
    }

    private void sessions() {
        for (Session session : server.node.getHubs()) {
            System.out.println(session);
        }
        for (Session session : server.node.getAgents()) {
            System.out.println(session);
        }
    }

    private void chains() {
        for (Session session : Stream.concat(server.node.getHubs().stream(), server.node.getAgents().stream()).toList()) {
            System.out.println(session);
            for (Chain chain : session.io.chainManager.storage().getAll()) {
                System.out.println(chain);
            }
        }

    }
}
