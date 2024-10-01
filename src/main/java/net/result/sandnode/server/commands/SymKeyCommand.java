package net.result.sandnode.server.commands;

import net.result.sandnode.server.Session;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.symmetric.SymmetricEncryptionFactory;
import net.result.sandnode.util.encryption.symmetric.aes.AESKeyConvertor;
import net.result.sandnode.util.encryption.symmetric.interfaces.SymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public class SymKeyCommand implements ICommand {

    private final Encryption encryption;
    private final SymmetricKeyStorage symmetricKeyStorage;

    public SymKeyCommand(@NotNull Encryption encryption, byte @NotNull [] symmetricKeyBytes) throws NoSuchAlgorithmException {
        this.encryption = encryption;
        SymmetricEncryptionFactory.getKeyConvertor(encryption);
        symmetricKeyStorage = AESKeyConvertor.getInstance().toKeyStorage(symmetricKeyBytes);
    }

    @Override
    public void execute(List<Session> sessionList, @NotNull Session session, @NotNull GlobalKeyStorage globalKeyStorage) {
        session.setKey(encryption, symmetricKeyStorage);
    }
}
