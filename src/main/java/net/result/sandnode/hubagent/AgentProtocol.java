package net.result.sandnode.hubagent;

import net.result.sandnode.chain.client.LogPasswdClientChain;
import net.result.sandnode.chain.client.LoginClientChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.chain.client.RegistrationClientChain;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.util.IOController;

public class AgentProtocol {
    public static String getTokenFromRegistration(IOController io, String memberID, String password)
            throws ExpectedMessageException, InterruptedException, SandnodeErrorException,
            UnknownSandnodeErrorException {
        RegistrationClientChain chain = new RegistrationClientChain(io, memberID, password);
        io.chainManager.linkChain(chain);
        chain.sync();
        io.chainManager.removeChain(chain);
        return chain.token;
    }

    public static String getMemberFromToken(IOController io, String token) throws InterruptedException,
            SandnodeErrorException, DeserializationException, ExpectedMessageException, UnknownSandnodeErrorException {
        LoginClientChain chain = new LoginClientChain(io, token);
        io.chainManager.linkChain(chain);
        chain.sync();
        io.chainManager.removeChain(chain);

        return chain.memberID;
    }

    public static String getTokenByMemberIdAndPassword(IOController io, String memberID, String password)
            throws InterruptedException, SandnodeErrorException, ExpectedMessageException,
            UnknownSandnodeErrorException {
        LogPasswdClientChain chain = new LogPasswdClientChain(io, memberID, password);
        io.chainManager.linkChain(chain);
        chain.sync();
        io.chainManager.removeChain(chain);
        return chain.token;
    }
}
