package net.result.sandnode.hubagent;

import net.result.sandnode.chain.sender.LogPasswdClientChain;
import net.result.sandnode.chain.sender.LoginClientChain;
import net.result.sandnode.chain.sender.RegistrationClientChain;
import net.result.sandnode.dto.LogPasswdResponseDTO;
import net.result.sandnode.dto.LoginResponseDTO;
import net.result.sandnode.dto.RegistrationResponseDTO;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.link.SandnodeLinkRecord;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.EncryptionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class AgentProtocol {
    public static final Logger LOGGER = LogManager.getLogger(AgentProtocol.class);

    public static RegistrationResponseDTO register(
            @NotNull SandnodeClient client,
            @NotNull String nickname,
            @NotNull String password,
            @NotNull String device
    ) throws InterruptedException, SandnodeErrorException, CannotUseEncryption, ProtocolException, StorageException {
        var chain = new RegistrationClientChain(client);
        client.io().chainManager.linkChain(chain);
        var dto = chain.register(nickname, password, device);
        client.io().chainManager.removeChain(chain);

        return dto;
    }

    public static LoginResponseDTO byToken(SandnodeClient client, String token)
            throws InterruptedException, SandnodeErrorException, ProtocolException {
        var chain = new LoginClientChain(client);
        client.io().chainManager.linkChain(chain);
        var dto = chain.login(token);
        client.io().chainManager.removeChain(chain);

        return dto;
    }

    public static LogPasswdResponseDTO byPassword(
            SandnodeClient client,
            String nickname,
            String password,
            String device
    ) throws InterruptedException, SandnodeErrorException, ProtocolException {
        var chain = new LogPasswdClientChain(client);
        client.io().chainManager.linkChain(chain);
        var dto = chain.getToken(nickname, password, device);
        client.io().chainManager.removeChain(chain);

        return dto;
    }

    public static AsymmetricKeyStorage loadOrFetchServerKey(SandnodeClient client, @NotNull SandnodeLinkRecord link)
            throws CryptoException, LinkDoesNotMatchException, InterruptedException, SandnodeErrorException,
            ProtocolException, StorageException {

        Agent agent = client.node().agent();

        AsymmetricKeyStorage filePublicKey = null;
        try {
            filePublicKey = agent.config.loadServerKey(link.address());
        } catch (KeyStorageNotFoundException ignored) {
        }
        AsymmetricKeyStorage linkKeyStorage = link.keyStorage();

        if (linkKeyStorage != null) {
            if (filePublicKey != null) {
                if (!EncryptionUtil.isPublicKeysEquals(filePublicKey, linkKeyStorage))
                    throw new LinkDoesNotMatchException("Key mismatch with saved configuration");

                LOGGER.info("Key already saved and matches");
                return filePublicKey;
            }

            agent.config.saveServerKey(link.address(), linkKeyStorage);
            return linkKeyStorage;
        }

        if (filePublicKey != null) return filePublicKey;

        ClientProtocol.PUB(client);
        AsymmetricEncryption encryption = client.io().serverEncryption().asymmetric();
        AsymmetricKeyStorage serverKey = agent.keyStorageRegistry.asymmetricNonNull(encryption);

        agent.config.saveServerKey(link.address(), serverKey);
        return serverKey;
    }
}
