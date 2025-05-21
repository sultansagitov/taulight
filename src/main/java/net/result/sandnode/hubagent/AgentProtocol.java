package net.result.sandnode.hubagent;

import net.result.sandnode.chain.sender.LogPasswdClientChain;
import net.result.sandnode.chain.sender.LoginClientChain;
import net.result.sandnode.chain.sender.RegistrationClientChain;
import net.result.sandnode.dto.LogPasswdResponseDTO;
import net.result.sandnode.dto.LoginResponseDTO;
import net.result.sandnode.dto.RegistrationResponseDTO;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.serverclient.SandnodeClient;
import org.jetbrains.annotations.NotNull;

public class AgentProtocol {
    public static RegistrationResponseDTO register(
            @NotNull SandnodeClient client,
            @NotNull String nickname,
            @NotNull String password,
            @NotNull String device,
            @NotNull AsymmetricKeyStorage keyStorage
    ) throws ExpectedMessageException, InterruptedException, SandnodeErrorException, UnknownSandnodeErrorException,
            UnprocessedMessagesException, DeserializationException, CannotUseEncryption {

        RegistrationClientChain chain = new RegistrationClientChain(client);
        client.io.chainManager.linkChain(chain);
        RegistrationResponseDTO dto = chain.register(nickname, password, device, keyStorage);
        client.io.chainManager.removeChain(chain);
        return dto;
    }

    public static LoginResponseDTO byToken(SandnodeClient client, String token)
            throws InterruptedException, SandnodeErrorException, DeserializationException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        LoginClientChain chain = new LoginClientChain(client);
        client.io.chainManager.linkChain(chain);
        LoginResponseDTO dto = chain.login(token);
        client.io.chainManager.removeChain(chain);

        return dto;
    }

    public static LogPasswdResponseDTO byPassword(SandnodeClient client, String nickname, String password, String device)
            throws InterruptedException, SandnodeErrorException, ExpectedMessageException,
            UnknownSandnodeErrorException, UnprocessedMessagesException, DeserializationException {
        LogPasswdClientChain chain = new LogPasswdClientChain(client);
        client.io.chainManager.linkChain(chain);
        LogPasswdResponseDTO token = chain.getToken(nickname, password, device);
        client.io.chainManager.removeChain(chain);
        return token;
    }
}
