package net.result.main;

import net.result.openhelo.messages.HeloMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Main {

    public static void main(String @NotNull [] args) throws Exception {
        String randomId = UUID.randomUUID().toString();
        System.setProperty("randomId", randomId);
        HeloMessageTypes.registerAll();

        if (args.length == 0) {
            System.out.println("Too few arguments");
            return;
        }

        WorkFactory.getWork(args[0]).run();
    }

}
