package net.result.main;

import net.result.sandnode.exception.ServerClosingException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.link.SandnodeLinkRecord;
import net.result.sandnode.serverclient.SandnodeServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class HubConsole {
    private static final Logger LOGGER = LogManager.getLogger(HubConsole.class);

    @FunctionalInterface
    interface F {
        void run() throws Exception;
    }

    private final SandnodeServer server;
    private final Map<String, F> commands = new HashMap<>();
    public boolean running = true;

    public HubConsole(SandnodeServer server) {
        this.server = server;
    }

    public void start() {
        commands.put("getlink", this::getLink);
        commands.put("exit", this::exit);

        System.out.println("Terminal started.");
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


    private void getLink() throws EncryptionTypeException, KeyStorageNotFoundException {
        URI link = SandnodeLinkRecord.fromServer(server).getURI();
        System.out.println("Link for server:");
        System.out.println();
        System.out.println(link);
        System.out.println();
    }

    private void exit() throws ServerClosingException {
        System.out.println("Shutting down...");
        running = false;
        server.close();
    }
}
