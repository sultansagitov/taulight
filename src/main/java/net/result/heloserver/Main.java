package net.result.heloserver;

import net.result.sandnode.config.ServerConfigSingleton;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

public class Main {

    public static void main(String @NotNull [] args) throws
            IOException, ReadingKeyException, GeneralSecurityException, NoSuchEncryptionException,
            EncryptionException, DecryptionException, NoSuchReqHandler, CreatingKeyException, CannotUseEncryption {
        String randomId = UUID.randomUUID().toString();
        System.setProperty("randomId", randomId);

        if (args.length == 0) {
            System.out.println("Too few arguments");
            return;
        }

        ServerConfigSingleton.getInstance();
        WorkFactory.getWork(args[0]).run();
    }

}
