package net.result.sandnode;

import net.result.sandnode.chain.client.LoginClientChain;
import net.result.sandnode.client.ClientMember;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.chain.client.RegistrationClientChain;
import net.result.sandnode.util.IOControl;
import org.jetbrains.annotations.NotNull;

public class AgentProtocol {
    public static String getTokenFromRegistration(
            @NotNull IOControl io,
            @NotNull String memberID,
            @NotNull String password
    ) throws ExpectedMessageException, InterruptedException, EncryptionTypeException, MemberNotFound,
            NoSuchEncryptionException, CreatingKeyException, KeyNotCreatedException, KeyStorageNotFoundException,
            DataNotEncryptedException {
        RegistrationClientChain regChain = new RegistrationClientChain(io, memberID, password);
        io.chainManager.addChain(regChain);
        regChain.sync();
        return regChain.token;
    }

    public static ClientMember getMemberFromToken(IOControl io, String token) throws EncryptionTypeException, MemberNotFound, NoSuchEncryptionException, ExpectedMessageException, CreatingKeyException, KeyNotCreatedException, KeyStorageNotFoundException, InterruptedException, DataNotEncryptedException {
        LoginClientChain chain = new LoginClientChain(io, token);
        io.chainManager.addChain(chain);
        chain.sync();

        return new ClientMember(chain.memberID);
    }
}
