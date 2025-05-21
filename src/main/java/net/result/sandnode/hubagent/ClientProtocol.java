package net.result.sandnode.hubagent;

import net.result.sandnode.exception.crypto.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.exception.*;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.sender.GroupClientChain;
import net.result.sandnode.chain.sender.PublicKeyClientChain;
import net.result.sandnode.chain.sender.SymKeyClientChain;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public class ClientProtocol {
    public static void PUB(@NotNull SandnodeClient client) throws CryptoException, ExpectedMessageException,
            InterruptedException, SandnodeErrorException, UnknownSandnodeErrorException, UnprocessedMessagesException {
        IOController io = client.io;
        PublicKeyClientChain pubkeyChain = new PublicKeyClientChain(client);
        io.chainManager.linkChain(pubkeyChain);
        pubkeyChain.getPublicKey();
        io.chainManager.removeChain(pubkeyChain);
    }

    public static void sendSYM(@NotNull SandnodeClient client) throws InterruptedException, ExpectedMessageException,
            KeyNotCreatedException, UnprocessedMessagesException {
        IOController io = client.io;
        SymKeyClientChain symKeyChain = new SymKeyClientChain(client);
        io.chainManager.linkChain(symKeyChain);
        symKeyChain.sendSymKey();
        io.chainManager.removeChain(symKeyChain);
    }

    public static Collection<String> addToGroups(@NotNull SandnodeClient client, Collection<String> groups)
            throws InterruptedException, ExpectedMessageException, UnprocessedMessagesException {
        IOController io = client.io;
        GroupClientChain groupClientChain = new GroupClientChain(client);
        io.chainManager.linkChain(groupClientChain);
        Collection<String> groupsID = groupClientChain.add(groups);
        io.chainManager.removeChain(groupClientChain);
        return groupsID;
    }

    public static Collection<String> getGroups(@NotNull SandnodeClient client)
            throws ExpectedMessageException, InterruptedException, UnprocessedMessagesException {
        return addToGroups(client, Set.of());
    }

    public static Collection<String> removeFromGroups(@NotNull SandnodeClient client, Collection<String> groups)
            throws InterruptedException, ExpectedMessageException, UnprocessedMessagesException {
        IOController io = client.io;
        GroupClientChain groupClientChain = new GroupClientChain(client);
        io.chainManager.linkChain(groupClientChain);
        Collection<String> groupsID = groupClientChain.remove(groups);
        io.chainManager.removeChain(groupClientChain);
        return groupsID;
    }
}
