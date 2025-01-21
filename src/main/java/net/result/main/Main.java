package net.result.main;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.taulight.TauErrors;
import net.result.taulight.messages.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Main {

    public static void main(String @NotNull [] args) throws Exception {
        String randomId = UUID.randomUUID().toString();
        System.setProperty("randomId", randomId);
        EncryptionManager.registerAll();
        TauMessageTypes.registerAll();
        TauErrors.registerAll();

        if (args.length == 0) {
            System.out.println("Too few arguments");
            return;
        }

        WorkFactory.getWork(args[0]).run();
    }

}
