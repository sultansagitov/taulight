package net.result.sandnode;

import net.result.sandnode.client.SandnodeClient;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.DecryptionException;
import net.result.sandnode.exceptions.EncryptionException;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.messages.types.RegistrationRequest;
import net.result.sandnode.messages.types.RegistrationResponse;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class AgentProtocol {
    @Contract("_, _, _ -> new")
    public static @NotNull RegistrationResponse registrationResponse(
            @NotNull SandnodeClient client,
            @NotNull String memberID,
            @NotNull String password
    ) throws UnexpectedSocketDisconnectException, EncryptionException, KeyStorageNotFoundException, NoSuchEncryptionException,
            DecryptionException, NoSuchMessageTypeException, ExpectedMessageException, MessageSerializationException,
            MessageWriteException {
        Headers set = new Headers().set(client.node.nodeConfig.symmetricKeyEncryption());
        RegistrationRequest request = new RegistrationRequest(set, memberID, password);
        client.io.sendMessage(request);

        return new RegistrationResponse(client.io.receiveMessage());
    }
}
