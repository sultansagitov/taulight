package net.result.sandnode;

import net.result.sandnode.client.SandnodeClient;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.util.IOControl;
import net.result.sandnode.chain.client.GroupClientChain;
import net.result.sandnode.chain.client.PublicKeyClientChain;
import net.result.sandnode.chain.client.SymKeyClientChain;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ClientProtocol {
    public static void PUB(@NotNull IOControl io) throws EncryptionTypeException, NoSuchEncryptionException,
            CreatingKeyException, ExpectedMessageException, InterruptedException {
        PublicKeyClientChain pubkeyChain = new PublicKeyClientChain(io);
        io.chainManager.linkChain(pubkeyChain);
        pubkeyChain.sync();
        io.chainManager.removeChain(pubkeyChain);
    }

    public static void sendSYM(@NotNull SandnodeClient client) throws InterruptedException, ExpectedMessageException,
            KeyNotCreatedException {
        SymKeyClientChain symkeyChain = new SymKeyClientChain(client.io, client.clientConfig.symmetricKeyEncryption());
        client.io.chainManager.linkChain(symkeyChain);
        symkeyChain.sync();
        client.io.chainManager.removeChain(symkeyChain);
    }

    public static Set<String> GROUP(@NotNull IOControl io, Set<String> groups) throws InterruptedException,
            ExpectedMessageException {
        GroupClientChain groupClientChain = new GroupClientChain(io, groups);
        io.chainManager.linkChain(groupClientChain);
        groupClientChain.sync();
        io.chainManager.removeChain(groupClientChain);
        return groupClientChain.groupNames;
    }
}
