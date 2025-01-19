package net.result.sandnode;

import net.result.sandnode.chain.client.LoginClientChain;
import net.result.sandnode.client.ClientMember;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.chain.client.RegistrationClientChain;
import net.result.sandnode.util.IOControl;

public class AgentProtocol {
    public static String getTokenFromRegistration(IOControl io, String memberID, String password)
            throws ExpectedMessageException, InterruptedException, BusyMemberIDException, DeserializationException,
            InvalidMemberIDPassword {
        RegistrationClientChain regChain = new RegistrationClientChain(io, memberID, password);
        io.chainManager.linkChain(regChain);
        regChain.sync();
        io.chainManager.removeChain(regChain);
        return regChain.token;
    }

    public static ClientMember getMemberFromToken(IOControl io, String token)
            throws InterruptedException, MemberNotFoundException, DeserializationException, InvalidTokenException {
        LoginClientChain loginClientChain = new LoginClientChain(io, token);
        io.chainManager.linkChain(loginClientChain);
        loginClientChain.sync();
        io.chainManager.removeChain(loginClientChain);

        return new ClientMember(loginClientChain.memberID);
    }
}
