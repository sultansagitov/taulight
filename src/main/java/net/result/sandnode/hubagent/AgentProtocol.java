package net.result.sandnode.hubagent;

import net.result.sandnode.chain.client.LogPasswdClientChain;
import net.result.sandnode.chain.client.LoginClientChain;
import net.result.sandnode.serverclient.ClientMember;
import net.result.sandnode.exception.*;
import net.result.sandnode.chain.client.RegistrationClientChain;
import net.result.sandnode.util.IOController;

public class AgentProtocol {
    public static String getTokenFromRegistration(IOController io, String memberID, String password)
            throws ExpectedMessageException, InterruptedException, BusyMemberIDException, DeserializationException,
            InvalidMemberIDPassword {
        RegistrationClientChain chain = new RegistrationClientChain(io, memberID, password);
        io.chainManager.linkChain(chain);
        chain.sync();
        io.chainManager.removeChain(chain);
        return chain.token;
    }

    public static ClientMember getMemberFromToken(IOController io, String token) throws InterruptedException,
            MemberNotFoundException, DeserializationException, InvalidTokenException, ExpiredTokenException {
        LoginClientChain chain = new LoginClientChain(io, token);
        io.chainManager.linkChain(chain);
        chain.sync();
        io.chainManager.removeChain(chain);

        return new ClientMember(chain.memberID);
    }

    public static String getTokenByMemberIdAndPassword(IOController io, String memberID, String password)
            throws InterruptedException, DeserializationException, MemberNotFoundException, ExpectedMessageException {
        LogPasswdClientChain chain = new LogPasswdClientChain(io, memberID, password);
        io.chainManager.linkChain(chain);
        chain.sync();
        io.chainManager.removeChain(chain);
        return chain.token;
    }
}
