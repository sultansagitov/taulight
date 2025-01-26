package net.result.sandnode.hubagent;

import net.result.sandnode.chain.client.LoginClientChain;
import net.result.sandnode.serverclient.ClientMember;
import net.result.sandnode.exception.*;
import net.result.sandnode.chain.client.RegistrationClientChain;
import net.result.sandnode.util.IOController;

public class AgentProtocol {
    public static String getTokenFromRegistration(IOController io, String memberID, String password)
            throws ExpectedMessageException, InterruptedException, BusyMemberIDException, DeserializationException,
            InvalidMemberIDPassword {
        RegistrationClientChain regChain = new RegistrationClientChain(io, memberID, password);
        io.chainManager.linkChain(regChain);
        regChain.sync();
        io.chainManager.removeChain(regChain);
        return regChain.token;
    }

    public static ClientMember getMemberFromToken(IOController io, String token)
            throws InterruptedException, MemberNotFoundException, DeserializationException, InvalidTokenException {
        LoginClientChain loginClientChain = new LoginClientChain(io, token);
        io.chainManager.linkChain(loginClientChain);
        loginClientChain.sync();
        io.chainManager.removeChain(loginClientChain);

        return new ClientMember(loginClientChain.memberID);
    }
}
