package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.PublicKeyDTO;
import net.result.sandnode.dto.RegisterRequestDTO;
import net.result.sandnode.dto.RegistrationResponseDTO;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.RegistrationRequest;
import net.result.sandnode.message.types.RegistrationResponse;
import net.result.sandnode.util.IOController;
import org.jetbrains.annotations.NotNull;

public class RegistrationClientChain extends ClientChain {
    public RegistrationClientChain(IOController io) {
        super(io);
    }

    public synchronized RegistrationResponseDTO register(
            @NotNull String nickname,
            @NotNull String password,
            @NotNull String device,
            @NotNull AsymmetricKeyStorage keyStorage
    ) throws InterruptedException, ExpectedMessageException, SandnodeErrorException, UnknownSandnodeErrorException,
            UnprocessedMessagesException, DeserializationException, CannotUseEncryption {

        var pubDTO = new PublicKeyDTO(keyStorage);
        var regDTO = new RegisterRequestDTO(nickname, password, device, pubDTO);

        RegistrationRequest request = new RegistrationRequest(regDTO);
        send(request);

        RawMessage response = queue.take();
        ServerErrorManager.instance().handleError(response);

        return new RegistrationResponse(response).dto();
    }
}
