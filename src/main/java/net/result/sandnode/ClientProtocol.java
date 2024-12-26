package net.result.sandnode;

import net.result.sandnode.chain.IChain;
import net.result.sandnode.client.SandnodeClient;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.util.IOControl;
import net.result.sandnode.chain.client.GroupClientChain;
import net.result.sandnode.chain.client.PublicKeyClientChain;
import net.result.sandnode.chain.client.SymKeyClientChain;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ClientProtocol {
    public static void PUB(@NotNull IOControl io) throws InterruptedException, EncryptionTypeException,
            NoSuchEncryptionException, CreatingKeyException, MemberNotFound, ExpectedMessageException,
            KeyNotCreatedException, KeyStorageNotFoundException, DataNotEncryptedException {
        IChain pubkeyChain = new PublicKeyClientChain(io);
        io.chainManager.addChain(pubkeyChain);
        pubkeyChain.sync();
    }

    public static void sendSYM(@NotNull SandnodeClient client) throws InterruptedException, KeyNotCreatedException,
            EncryptionTypeException, MemberNotFound, NoSuchEncryptionException, ExpectedMessageException,
            CreatingKeyException, KeyStorageNotFoundException, DataNotEncryptedException {
        IChain symkeyChain = new SymKeyClientChain(client.io, client.clientConfig.symmetricKeyEncryption());
        client.io.chainManager.addChain(symkeyChain);
        symkeyChain.sync();
    }

    public static Set<String> GROUP(@NotNull IOControl io, Set<String> groups) throws ExpectedMessageException,
            InterruptedException, EncryptionTypeException, MemberNotFound, NoSuchEncryptionException,
            CreatingKeyException, KeyNotCreatedException, KeyStorageNotFoundException, DataNotEncryptedException {
        GroupClientChain groupClientChain = new GroupClientChain(io, groups);
        io.chainManager.addChain(groupClientChain);
        groupClientChain.sync();
        return groupClientChain.groupNames;
    }
}
