package net.result.sandnode.hubagent;

import net.result.sandnode.chain.sender.LogPasswdClientChain;
import net.result.sandnode.chain.sender.LoginClientChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.chain.sender.RegistrationClientChain;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.util.IOController;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class AgentProtocol {
    public static String getTokenFromRegistration(
            @NotNull IOController io,
            @NotNull String nickname,
            @NotNull String password,
            @NotNull String device,
            @NotNull Map<String, String> keyStorage
    ) throws ExpectedMessageException, InterruptedException, SandnodeErrorException, UnknownSandnodeErrorException,
            UnprocessedMessagesException {
        RegistrationClientChain chain = new RegistrationClientChain(io);
        io.chainManager.linkChain(chain);
        String token = chain.getTokenFromRegistration(nickname, password, device, keyStorage);
        io.chainManager.removeChain(chain);
        return token;
    }

    public static String getMemberFromToken(IOController io, String token)
            throws InterruptedException, SandnodeErrorException, DeserializationException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        LoginClientChain chain = new LoginClientChain(io);
        io.chainManager.linkChain(chain);
        String nickname = chain.getNickname(token);
        io.chainManager.removeChain(chain);

        return nickname;
    }

    public static String getTokenByNicknameAndPassword(IOController io, String nickname, String password, String device)
            throws InterruptedException, SandnodeErrorException, ExpectedMessageException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        LogPasswdClientChain chain = new LogPasswdClientChain(io);
        io.chainManager.linkChain(chain);
        String token = chain.getToken(nickname, password, device);
        io.chainManager.removeChain(chain);
        return token;
    }
}
