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
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        RegistrationClientChain chain = new RegistrationClientChain(io);
        io.chainManager.linkChain(chain);
        String token = chain.getTokenFromRegistration(memberID, password);
        io.chainManager.removeChain(chain);
        return token;
    }

    public static String getMemberFromToken(IOController io, String token)
            throws InterruptedException, SandnodeErrorException, DeserializationException, ExpectedMessageException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        LoginClientChain chain = new LoginClientChain(io);
        io.chainManager.linkChain(chain);
        String memberID = chain.getMemberID(token);
        io.chainManager.removeChain(chain);

        return memberID;
    }

    public static String getTokenByMemberIdAndPassword(IOController io, String memberID, String password)
            throws InterruptedException, SandnodeErrorException, ExpectedMessageException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        LogPasswdClientChain chain = new LogPasswdClientChain(io);
        io.chainManager.linkChain(chain);
        String token = chain.getToken(memberID, password);
        io.chainManager.removeChain(chain);
        return token;
    }
}
