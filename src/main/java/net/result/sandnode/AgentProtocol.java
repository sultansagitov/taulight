package net.result.sandnode;

import net.result.sandnode.client.SandnodeClient;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.chain.client.RegistrationClientChain;
import org.jetbrains.annotations.NotNull;

public class AgentProtocol {
    public static String getTokenFromRegistration(
            @NotNull SandnodeClient client,
            @NotNull String memberID,
            @NotNull String password
    ) throws ExpectedMessageException, InterruptedException, EncryptionTypeException, MemberNotFound,
            NoSuchEncryptionException, CreatingKeyException, KeyNotCreatedException, KeyStorageNotFoundException,
            DataNotEncryptedException {
        RegistrationClientChain regChain = new RegistrationClientChain(client.io, memberID, password);
        client.io.chainManager.addChain(regChain);
        regChain.sync();
        return regChain.token;
    }
}
