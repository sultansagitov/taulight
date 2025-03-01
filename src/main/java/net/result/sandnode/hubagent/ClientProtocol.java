package net.result.sandnode.hubagent;

import net.result.sandnode.exception.crypto.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.exception.*;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.client.GroupClientChain;
import net.result.sandnode.chain.client.PublicKeyClientChain;
import net.result.sandnode.chain.client.SymKeyClientChain;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public class ClientProtocol {
    public static void PUB(@NotNull IOController io) throws CryptoException, ExpectedMessageException,
            InterruptedException, SandnodeErrorException, UnknownSandnodeErrorException {
        PublicKeyClientChain pubkeyChain = new PublicKeyClientChain(io);
        io.chainManager.linkChain(pubkeyChain);
        pubkeyChain.sync();
        io.chainManager.removeChain(pubkeyChain);
    }

    public static void sendSYM(@NotNull SandnodeClient client)
            throws InterruptedException, ExpectedMessageException, KeyNotCreatedException {
        IOController io = client.io;
        SymKeyClientChain symKeyChain = new SymKeyClientChain(io, client.clientConfig.symmetricKeyEncryption());
        io.chainManager.linkChain(symKeyChain);
        symKeyChain.sync();
        io.chainManager.removeChain(symKeyChain);
    }

    public static Collection<String> addToGroups(@NotNull IOController io, Collection<String> groups)
            throws InterruptedException, ExpectedMessageException {
        GroupClientChain groupClientChain = new GroupClientChain(io, groups);
        io.chainManager.linkChain(groupClientChain);
        groupClientChain.sync();
        io.chainManager.removeChain(groupClientChain);
        return groupClientChain.groupsID;
    }

    public static Collection<String> getGroups(@NotNull IOController io)
            throws ExpectedMessageException, InterruptedException {
        return addToGroups(io, Set.of());
    }

    public static Collection<String> removeFromGroups(@NotNull IOController io, Collection<String> groups)
            throws InterruptedException, ExpectedMessageException {
        GroupClientChain groupClientChain = GroupClientChain.remove(io, groups);
        io.chainManager.linkChain(groupClientChain);
        groupClientChain.sync();
        io.chainManager.removeChain(groupClientChain);
        return groupClientChain.groupsID;
    }
}
