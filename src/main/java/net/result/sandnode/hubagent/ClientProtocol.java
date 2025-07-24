package net.result.sandnode.hubagent;

import net.result.sandnode.exception.crypto.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.exception.*;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.sender.ClusterClientChain;
import net.result.sandnode.chain.sender.PublicKeyClientChain;
import net.result.sandnode.chain.sender.SymKeyClientChain;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public class ClientProtocol {
    public static void PUB(@NotNull SandnodeClient client)
            throws CryptoException, ProtocolException, InterruptedException, SandnodeErrorException {
        IOController io = client.io();
        PublicKeyClientChain pubkeyChain = new PublicKeyClientChain(client);
        io.chainManager.linkChain(pubkeyChain);
        pubkeyChain.getPublicKey();
        io.chainManager.removeChain(pubkeyChain);
    }

    public static void sendSYM(@NotNull SandnodeClient client)
            throws InterruptedException, ProtocolException, KeyNotCreatedException, SandnodeErrorException {
        IOController io = client.io();
        SymKeyClientChain symKeyChain = new SymKeyClientChain(client);
        io.chainManager.linkChain(symKeyChain);
        symKeyChain.sendSymKey();
        io.chainManager.removeChain(symKeyChain);
    }

    public static Collection<String> addToClusters(@NotNull SandnodeClient client, Collection<String> clusters)
            throws InterruptedException, ProtocolException, SandnodeErrorException {
        IOController io = client.io();
        ClusterClientChain chain = new ClusterClientChain(client);
        io.chainManager.linkChain(chain);
        Collection<String> clustersID = chain.add(clusters);
        io.chainManager.removeChain(chain);
        return clustersID;
    }

    public static Collection<String> getClusters(@NotNull SandnodeClient client)
            throws ProtocolException, InterruptedException, SandnodeErrorException {
        return addToClusters(client, Set.of());
    }

    public static Collection<String> removeFromClusters(@NotNull SandnodeClient client, Collection<String> clusters)
            throws InterruptedException, ProtocolException, SandnodeErrorException {
        IOController io = client.io();
        ClusterClientChain chain = new ClusterClientChain(client);
        io.chainManager.linkChain(chain);
        Collection<String> clustersID = chain.remove(clusters);
        io.chainManager.removeChain(chain);
        return clustersID;
    }
}
